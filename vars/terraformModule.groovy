import com.opstree.terraform.TerraformUtils

def call(Map config = [:]) {
    // Validate and sanitize inputs
    TerraformUtils.validateEnvironment(config)
    config = TerraformUtils.sanitizeInputs(config)

    // Create and execute pipeline
    def pipeline = new com.opstree.terraform.TerraformPipeline(this, config)
    pipeline.execute()
}
