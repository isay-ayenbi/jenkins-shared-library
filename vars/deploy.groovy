
def call(type, project, ip, buildNumber, environment, branchName) {
    echo "Parameters: ${type} ${project} ${ip} ${buildNumber} ${environment} ${branchName}"
    sh "/home/tomcat/promote_build.sh ${type} ${project} ${ip} ${buildNumber} ${environment} ${branchName}"
}
