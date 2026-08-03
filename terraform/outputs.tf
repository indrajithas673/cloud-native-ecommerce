output "instance_id" {
  description = "EC2 instance ID for the k3s node."
  value       = aws_instance.k3s.id
}

output "public_ip" {
  description = "Elastic IP attached to the k3s node."
  value       = aws_eip.k3s.public_ip
}

output "public_dns" {
  description = "Public DNS name of the EC2 instance."
  value       = aws_instance.k3s.public_dns
}

output "ssh_command" {
  description = "Convenient SSH command for the Ubuntu host."
  value       = "ssh ubuntu@${aws_eip.k3s.public_ip}"
}

output "kubeconfig_copy_command" {
  description = "Command to copy the cluster kubeconfig locally after SSH access is working."
  value       = "scp ubuntu@${aws_eip.k3s.public_ip}:/etc/rancher/k3s/k3s.yaml ./k3s.yaml"
}

output "k8s_runtime_values_file" {
  description = "Generated env file consumed by Kustomize for ingress hosts and Keycloak issuer URI."
  value       = local_file.k8s_runtime_values.filename
}
