provider "azurerm" {
  version = "=1.19.0"
}

# Make sure the resource group exists
resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = "${var.location_app}"
}

provider "vault" {
  address = "https://vault.reform.hmcts.net:6200"
}

data "azurerm_key_vault" "sscs_key_vault" {
  name                = "${local.azureVaultName}"
  resource_group_name = "${local.azureVaultName}"
}

data "azurerm_key_vault_secret" "sscs-s2s-secret" {
  name = "sscs-s2s-secret"
  vault_uri = "${data.azurerm_key_vault.sscs_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam-sscs-systemupdate-user" {
  name = "idam-sscs-systemupdate-user"
  vault_uri = "${data.azurerm_key_vault.sscs_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam-sscs-systemupdate-password" {
  name = "idam-sscs-systemupdate-password"
  vault_uri = "${data.azurerm_key_vault.sscs_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam-sscs-oauth2-client-secret" {
  name = "idam-sscs-oauth2-client-secret"
  vault_uri = "${data.azurerm_key_vault.sscs_key_vault.vault_uri}"
}

locals {
  ase_name = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"

  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  local_ase = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "core-compute-aat" : "core-compute-saat" : local.ase_name}"

  s2sCnpUrl = "http://rpe-service-auth-provider-${local.local_env}.service.${local.local_ase}.internal"
  cohUrl    = "http://coh-cor-${local.local_env}.service.${local.local_ase}.internal"
  ccdApi    = "http://ccd-data-store-api-${local.local_env}.service.${local.local_ase}.internal"
  idam_url  = "https://preprod-idamapi.reform.hmcts.net:3511"
  documentManagementUrl = "http://dm-store-${local.local_env}.service.${local.local_ase}.internal"
  pdfService    = "http://cmc-pdf-service-${local.local_env}.service.${local.local_ase}.internal"
  notificationsApiUrl = "http://sscs-tya-notif-${local.local_env}.service.${local.local_ase}.internal"

  azureVaultName = "sscs-${local.local_env}"

  createCcdEndpoint = "${(var.env == "preview" || var.env == "spreview" ||  var.env == "aat") ? "true" : "false"}"
}

module "sscs-core-backend" {
  source              = "git@github.com:hmcts/moj-module-webapp?ref=master"
  product             = "${var.product}-${var.component}"
  location            = "${var.location_app}"
  env                 = "${var.env}"
  ilbIp               = "${var.ilbIp}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  subscription        = "${var.subscription}"
  capacity            = "${var.capacity}"
  common_tags         = "${var.common_tags}"
  asp_rg              = "${var.product}-${var.component}-${var.env}"
  asp_name            = "${var.product}-${var.component}-${var.env}"

  app_settings = {
    IDAM_S2S_AUTH                   = "${local.s2sCnpUrl}"
    IDAM_S2S_AUTH_TOTP_SECRET       = "${data.azurerm_key_vault_secret.sscs-s2s-secret.value}"
    IDAM_SSCS_SYSTEMUPDATE_USER     = "${data.azurerm_key_vault_secret.idam-sscs-systemupdate-user.value}"
    IDAM_SSCS_SYSTEMUPDATE_PASSWORD = "${data.azurerm_key_vault_secret.idam-sscs-systemupdate-password.value}"
    IDAM_OAUTH2_CLIENT_SECRET       = "${data.azurerm_key_vault_secret.idam-sscs-oauth2-client-secret.value}"
    IDAM_OAUTH2_REDIRECT_URL        = "${var.idam_redirect_url}"
    IDAM_URL                        = "${local.idam_url}"
    IDAM_SSCS_URL                   = "${var.idam_sscs_url}"

    COH_URL = "${local.cohUrl}"
    CORE_CASE_DATA_URL = "${local.ccdApi}"

    CREATE_CCD_ENDPOINT = "${local.createCcdEndpoint}"

    DOCUMENT_MANAGEMENT_URL = "${local.documentManagementUrl}"
    PDF_API_URL = "${local.pdfService}"
    NOTIFICATIONS_API_URL = "${local.notificationsApiUrl}"
    ENABLE_DEBUG_ERROR_MESSAGE = "${var.enable_debug_error_message}"
  }
}
