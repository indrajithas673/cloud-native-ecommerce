data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }
}

resource "aws_vpc" "k3s" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags = {
    Name = "${var.project_name}-vpc"
  }
}

resource "aws_internet_gateway" "k3s" {
  vpc_id = aws_vpc.k3s.id
  tags = {
    Name = "${var.project_name}-igw"
  }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.k3s.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.k3s.id
  }

  tags = {
    Name = "${var.project_name}-rt"
  }
}

resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.k3s.id
  cidr_block              = var.public_subnet_cidr
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.project_name}-subnet-public"
  }
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

resource "aws_security_group" "k3s_sg" {
  name        = "${var.project_name}-k3s-sg"
  description = "Security group for k3s cluster"
  vpc_id      = aws_vpc.k3s.id

  ingress {
    description = "SSH access"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.admin_cidr]
  }

  ingress {
    description = "HTTP access"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS access"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "K3s API access"
    from_port   = 6443
    to_port     = 6443
    protocol    = "tcp"
    cidr_blocks = [var.admin_cidr]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-sg"
  }
}

resource "aws_instance" "k3s" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = var.instance_type
  key_name               = var.key_name
  vpc_security_group_ids = [aws_security_group.k3s_sg.id]
  subnet_id              = aws_subnet.public.id

  root_block_device {
    volume_size = var.root_volume_size
    volume_type = "gp3"
  }

  user_data = <<-EOF
                #!/bin/bash
                export INSTALL_K3S_VERSION="v1.30.2+k3s1"
                apt-get update && apt-get install -y amazon-ecr-credential-helper
                mkdir -p /etc/rancher/k3s
                cat << 'EOF2' > /etc/rancher/k3s/registries.yaml
                configs:
                  "035466343449.dkr.ecr.eu-north-1.amazonaws.com":
                    auth:
                      username: AWS
                      password: ""
                EOF2
                curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC="--cluster-cidr=10.244.0.0/16 --service-cidr=10.245.0.0/16" sh -
                EOF

  user_data_replace_on_change = true

  tags = {
    Name = "${var.project_name}-k3s-node"
  }
}

resource "aws_eip" "k3s" {
  instance = aws_instance.k3s.id
  domain   = "vpc"

  tags = {
    Name = "${var.project_name}-eip"
  }
}

# Generate passwords
resource "random_password" "mysql" {
  length           = 16
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

resource "random_password" "keycloak" {
  length           = 16
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

resource "random_password" "product_service" {
  length           = 16
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

resource "random_password" "order_service" {
  length           = 16
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

resource "random_password" "inventory_service" {
  length           = 16
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

resource "random_password" "notification_service" {
  length           = 16
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

resource "random_password" "grafana" {
  length           = 16
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

# Create a local env file that Kustomize will consume (e.g., k8s/generated/runtime-values.env)
resource "local_file" "k8s_runtime_values" {
  content  = <<-EOT
PUBLIC_IP=${aws_eip.k3s.public_ip}
API_HOST=api.${aws_eip.k3s.public_ip}.nip.io
AUTH_HOST=auth.${aws_eip.k3s.public_ip}.nip.io
KEYCLOAK_ISSUER_URI=http://auth.${aws_eip.k3s.public_ip}.nip.io/realms/spring-boot-microservices-realm
DB_HOST=${aws_db_instance.rds.address}
EOT
  filename = "${path.module}/../k8s/generated/runtime-values.env"
}

# Save passwords securely locally and exclude from source control
resource "local_file" "generated_secrets" {
  content         = <<-EOT
MYSQL_PASSWORD=${random_password.mysql.result}
KEYCLOAK_PASSWORD=${random_password.keycloak.result}
PRODUCT_DB_PASSWORD=${random_password.product_service.result}
ORDER_DB_PASSWORD=${random_password.order_service.result}
INVENTORY_DB_PASSWORD=${random_password.inventory_service.result}
NOTIFICATION_DB_PASSWORD=${random_password.notification_service.result}
GRAFANA_PASSWORD=${random_password.grafana.result}
DB_PORT=3306
EOT
  filename        = "${path.module}/../k8s/generated/generated-secrets.env"
  file_permission = "0600"
}

# --- ECR Repositories ---

locals {
  services = [
    "api-gateway",
    "product-service",
    "order-service",
    "inventory-service",
    "notification-service"
  ]
}

resource "aws_ecr_repository" "microservices" {
  for_each             = toset(local.services)
  name                 = "${var.project_name}/${each.key}"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name = "${var.project_name}-${each.key}-repo"
  }
}

# --- RDS Configuration ---
data "aws_availability_zones" "available" {
  state = "available"
}

resource "aws_subnet" "private_1" {
  vpc_id            = aws_vpc.k3s.id
  cidr_block        = var.private_subnet_1_cidr
  availability_zone = data.aws_availability_zones.available.names[0]

  tags = {
    Name = "${var.project_name}-subnet-private-1"
  }
}

resource "aws_subnet" "private_2" {
  vpc_id            = aws_vpc.k3s.id
  cidr_block        = var.private_subnet_2_cidr
  availability_zone = data.aws_availability_zones.available.names[1]

  tags = {
    Name = "${var.project_name}-subnet-private-2"
  }
}

resource "aws_db_subnet_group" "rds" {
  name       = "${var.project_name}-rds-subnet-group"
  subnet_ids = [aws_subnet.private_1.id, aws_subnet.private_2.id]

  tags = {
    Name = "${var.project_name}-rds-subnet-group"
  }
}

resource "aws_security_group" "rds_sg" {
  name        = "${var.project_name}-rds-sg"
  description = "Security group for RDS instance"
  vpc_id      = aws_vpc.k3s.id

  ingress {
    description     = "MySQL access from K3s EC2"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.k3s_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-rds-sg"
  }
}

resource "aws_db_instance" "rds" {
  identifier           = "${var.project_name}-db"
  engine               = "mysql"
  engine_version       = "8.0"
  instance_class       = "db.t3.micro"
  allocated_storage    = 20
  storage_type         = "gp3"
  username             = "root"
  password             = random_password.mysql.result
  db_subnet_group_name = aws_db_subnet_group.rds.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]
  publicly_accessible  = false
  skip_final_snapshot  = true
  apply_immediately    = true

  tags = {
    Name = "${var.project_name}-rds"
  }
}
