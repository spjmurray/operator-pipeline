def call(String suite) {
  // The pipeline shouldn't have to know about, or be tied to the internal implementation
  // of the test framework, so abstract that here.
  def suiteMapping = [
    'validation': 'TestCRDValidation',
    'sanity': 'TestSanity',
    'p0': 'TestP0',
    'p1': 'TestP1',
    'custom': 'TestCustom',
  ]

  // Clean out any stale logs or junit defintitions.
  // TODO: this probably makes more sense as a makefile command.
  sh 'rm -rf test/e2e/logs test/e2e/*.xml'

  // Run the test.  The expectation is that our suites are unstable and will
  // crash and burn more often than not.  We want to carry on regardless.
  script {
    try {
      sh "go test github.com/couchbase/couchbase-operator/test/e2e -run TestOperator -v -race -timeout 12h -args -platform-type ${params.platform} -operator-image ${env.DOCKER_CREDENTIALS_USR}/couchbase-operator:`git rev-parse FETCH_HEAD` -admission-image ${env.DOCKER_CREDENTIALS_USR}/couchbase-operator-admission:`git rev-parse FETCH_HEAD` -mobile-image ${params.mobile_image} -server-image ${params.server_image} -server-image-upgrade ${params.server_image_upgrade} -exporter-image ${params.exporter_image} -exporter-image-upgrade ${params.exporter_image_upgrade} -backup-image ${params.backup_image} -suite ${suiteMapping[suite]} -storage-class ${params.storage_class} -kubeconfig1 ${params.kubeconfig1} -kubeconfig2 ${params.kubeconfig2} -docker-username ${env.DOCKER_CREDENTIALS_USR} -docker-password ${env.DOCKER_CREDENTIALS_PSW} -collect-logs"
    }
    catch (e) {
      currentBuild.result = 'UNSTABLE'
    }
  }
}
