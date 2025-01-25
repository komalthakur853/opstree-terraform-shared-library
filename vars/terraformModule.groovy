def call(Map config = [:]) {
    def pipeline = new com.opstree.terraform.TerraformPipeline(this, config)
    pipeline.execute()
}
