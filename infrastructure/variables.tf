variable "product" {
  type    = "string"
}

variable "component" {
  type = "string"
}

variable "location_app" {
  type    = "string"
  default = "UK South"
}

variable "env" {
  type = "string"
}

variable "ilbIp" {}

variable "subscription" {}

variable "capacity" {
  default = "1"
}

variable "common_tags" {
  type = "map"
}

variable "idam_redirect_url" {
  default = "https://sscs-case-loader-sandbox.service.core-compute-sandbox.internal"
}

variable "idam_sscs_url" {
  default = "https://evidence-sharing-preprod.sscs.reform.hmcts.net"
}

variable "infrastructure_env" {
  default     = "test"
  description = "Infrastructure environment to point to"
}

variable "enable_debug_error_message" {
  default     = "true"
  description = "Enable stack traces on error messages"
}

variable "enable_select_by_case_id" {
  default     = "false"
}

variable "dwp_email" {
  default = "dwp_email@example.com"
}

variable "caseworker_email_address" {
  default = "caseworker@example.net"
}

variable "email_from_address" {
  default = "sscs@hmcts.net"
}

variable "createCcdEndpoint" {
  default     = "false"
}

variable "jui_base_url" {
  type = "string"
}

variable "appinsights_instrumentation_key" {
  type = "string"
}