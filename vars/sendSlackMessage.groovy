def call(Map config = [:]) {
    // Determine build status color
    def buildColor = (config.status == 'SUCCESS') ? '#00FF00' : '#FF0000'
    
    // Send the message using the Slack plugin step
    slackSend(
        channel: config.channel ?: '#jenkins',
        color: buildColor,
        message: "${config.status}: Job '${env.JOB_NAME}' [Build #${env.BUILD_NUMBER}] (${env.BUILD_URL})"
    )
}