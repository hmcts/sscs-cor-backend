provider "azurerm" {
  version = "=1.19.0"
}

# Make sure the resource group exists
resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = "${var.location_app}"
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

data "azurerm_key_vault_secret" "docmosis-api-key" {
  name = "docmosis-api-key"
  vault_uri = "${data.azurerm_key_vault.sscs_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "appinsights_instrumentation_key" {
  name      = "AppInsightsInstrumentationKey"
  vault_uri = "${data.azurerm_key_vault.sscs_key_vault.vault_uri}"
}

locals {
  ase_name = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"

  local_idam_env = "${var.env == "prod" ? "" : ".${var.env}"}"

  s2sCnpUrl = "http://rpe-service-auth-provider-${var.env}.service.${local.ase_name}.internal"
  cohUrl    = "http://coh-cor-${var.env}.service.${local.ase_name}.internal"
  ccdApi    = "http://ccd-data-store-api-${var.env}.service.${local.ase_name}.internal"
  idam_url  = "https://idam-api${local.local_idam_env}.platform.hmcts.net"
  documentManagementUrl = "http://dm-store-${var.env}.service.${local.ase_name}.internal"
  pdfService    = "http://cmc-pdf-service-${var.env}.service.${local.ase_name}.internal"
  notificationsApiUrl = "http://sscs-tya-notif-${var.env}.service.${local.ase_name}.internal"

  azureVaultName = "sscs-${var.env}"

  email_host      = "mta.reform.hmcts.net"
  email_port      = "25"
}

module "sscs-core-backend" {
  source              = "git@github.com:hmcts/cnp-module-webapp?ref=master"
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
    APPINSIGHTS_INSTRUMENTATIONKEY = "${data.azurerm_key_vault_secret.appinsights_instrumentation_key.value}"
    
    COH_URL = "${local.cohUrl}"
    CORE_CASE_DATA_URL = "${local.ccdApi}"

    CREATE_CCD_ENDPOINT = "${var.createCcdEndpoint}"

    DOCUMENT_MANAGEMENT_URL = "${local.documentManagementUrl}"
    PDF_API_URL = "${local.pdfService}"
    NOTIFICATIONS_API_URL = "${local.notificationsApiUrl}"
    ENABLE_DEBUG_ERROR_MESSAGE = "${var.enable_debug_error_message}"
    ENABLE_SELECT_BY_CASE_ID = "${var.enable_select_by_case_id}"

    EMAIL_SERVER_HOST      = "${local.email_host}"
    EMAIL_SERVER_PORT      = "${local.email_port}"
    DWP_EMAIL              = "${var.dwp_email}"
    EMAIL_FROM             = "${var.email_from_address}"
    CASEWORKER_EMAIL       = "${var.caseworker_email_address}"

    PDF_SERVICE_ACCESS_KEY = "${data.azurerm_key_vault_secret.docmosis-api-key.value}"

    JUI_BASE_URL  = "${var.jui_base_url}"
  }
}
