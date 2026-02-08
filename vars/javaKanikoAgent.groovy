// def call(Map config = [:], Closure body) {
def call(Map config = [:]) {
  def mavenImage  = config.mavenImage ?: "maven:3.9.6-eclipse-temurin-17"
  def kanikoImage = config.kanikoImage ?: "gcr.io/kaniko-project/executor:debug"

  return """
apiVersion: v1
kind: Pod
spec:
  volumes:
    - name: maven-settings
      configMap:
        name: maven-settings
    - name: maven-repo-cache
      emptyDir: {}

  containers:
  - name: maven
    image: ${mavenImage}
    command: ["cat"]
    tty: true
    volumeMounts:
      - name: maven-settings
        mountPath: /root/.m2/settings.xml
        subPath: settings.xml
      - name: maven-repo-cache
        mountPath: /root/.m2/repository

  - name: kaniko
    image: ${kanikoImage}
    command: ["sh","-c","cat"]
    tty: true

  - name: jnlp
    image: jenkins/inbound-agent:latest
""" {

    node(POD_LABEL) {
      body()
    }
  }
}
