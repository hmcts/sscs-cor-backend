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

output "idam_redirect_url" {
  value = "${var.idam_redirect_url}"
}

output "pdf_service_health_url" {
  value = "${var.pdf_service_health_url}rs/render"
}

output "pdf_service_convert_url" {
  value = "${var.pdf_service_convert_url}rs/convert"
}

output "pdf_service_base_url" {
  value = "${var.pdf_service_base_url}rs/render"
}
