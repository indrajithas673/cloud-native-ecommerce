variable "aws_region" {
  description = "AWS region for the single-node k3s host."
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Prefix used for Terraform-managed resource names."
  type        = string
  default     = "micro-marketplace"
}

variable "environment" {
  description = "Environment tag value."
  type        = string
  default     = "dev"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC."
  type        = string
  default     = "10.42.0.0/16"
}

variable "public_subnet_cidr" {
  description = "CIDR block for the public subnet."
  type        = string
  default     = "10.42.1.0/24"
}

variable "instance_type" {
  description = "EC2 instance type. Keep this on a Free Tier-eligible micro size where available."
  type        = string
  default     = "t3.micro"
}

variable "root_volume_size" {
  description = "Root EBS volume size in GiB."
  type        = number
  default     = 20
}

variable "key_name" {
  description = "Existing AWS EC2 key pair name for SSH access."
  type        = string
}

variable "admin_cidr" {
  description = "CIDR allowed to access SSH and the k3s API. Use your public IP with /32."
  type        = string
}
