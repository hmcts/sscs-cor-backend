output "microserviceName" {
  value = "${var.component}"
}

output "vaultName" {
  value = "${local.azureVaultName}"
}

output "idam_s2s_auth" {
  value = "http://rpe-service-auth-provider-aat.service.core-compute-aat.internal"
}

output "coh_url" {
  value = "http://coh-cor-aat.service.core-compute-aat.internal"
}
