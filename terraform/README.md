# Terraform Deployment Instructions

This Terraform stack creates a low-cost single-node AWS environment for the k3s deployment:

- 1 VPC
- 1 public subnet
- 1 internet gateway
- 1 public route table
- 1 security group
- 1 Ubuntu EC2 instance
- 1 Elastic IP

It also bootstraps k3s automatically on the EC2 instance with `user_data`.

## Cost notes

- Keep `instance_type = "t3.micro"` if it is Free Tier-eligible for your account and region.
- AWS states that new accounts now begin with Free Tier credits and free usage eligibility varies by plan and service.
- Elastic IP addresses are billed by AWS, including in-use addresses, so this stack is not truly `$0` forever.
- EBS root volume charges can also apply once free credits or free usage are exhausted.

Sources:
- AWS Free Tier: https://aws.amazon.com/free/
- EC2 pricing / Elastic IP pricing note: https://aws.amazon.com/ec2/pricing/on-demand/

## Prerequisites

1. Install Terraform.
2. Install AWS CLI and run `aws configure`.
3. Create or reuse an AWS EC2 key pair in the target region.
4. Copy `terraform.tfvars.example` to `terraform.tfvars` and fill in:
   - `key_name`
   - `admin_cidr`
   - any optional overrides

## Deploy

From the `terraform/` directory:

```bash
terraform init
terraform plan
terraform apply
```

## Verify the host

After apply finishes:

```bash
ssh ubuntu@<elastic-ip>
sudo kubectl get nodes
```

The node should report `Ready`.

## Copy kubeconfig locally

```bash
scp ubuntu@<elastic-ip>:/etc/rancher/k3s/k3s.yaml ./k3s.yaml
sed -i 's/127.0.0.1/<elastic-ip>/g' ./k3s.yaml
export KUBECONFIG=$PWD/k3s.yaml
kubectl get nodes
```

On Windows PowerShell, replace the `sed` step with:

```powershell
(Get-Content .\k3s.yaml) -replace '127.0.0.1', '<elastic-ip>' | Set-Content .\k3s.yaml
$env:KUBECONFIG = "$PWD\k3s.yaml"
kubectl get nodes
```

## Deploy the app manifests

1. Update passwords in `../k8s/secrets.yaml`.
2. Run `terraform apply`.
3. Terraform will generate `../k8s/generated/runtime-values.env` automatically.
4. Apply the Kubernetes manifests:

```bash
kubectl kustomize --load-restrictor LoadRestrictionsNone ../k8s | kubectl apply -f -
```

## Recommended AWS cleanup

To avoid charges, destroy the stack when you are done:

```bash
terraform destroy
```
