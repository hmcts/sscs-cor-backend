output "microserviceName" {
  value = "${var.component}"
}

output "vaultName" {
  value = "${local.azureVaultName}"
}

output "vaultUri" {
  value = "${data.azurerm_key_vault.sscs_key_vault.vault_uri}"
}

output "idam_s2s_auth" {
  value = "http://rpe-service-auth-provider-aat.service.core-compute-aat.internal"
}

output "coh_url" {
  value = "http://coh-cor-aat.service.core-compute-aat.internal"
}