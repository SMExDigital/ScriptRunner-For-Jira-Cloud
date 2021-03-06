// ********************************
// This groovy script removes unwanted words from the summary
// This happens due to emails being turned into issues
//
// Created By: Mike Burns
// Last Updated By: Mike Burns
//*********************************

logger.trace("Event -> ${issue_event_type_name}")

def summary = issue.fields.summary
while (summary != null) {
    def madeChange = false

    try {
        ["not urgent", "Semi-Urgent", "URGENT"].each { String token ->
            if (summary != summary.replaceAll("(?i)" + token, " ")) {
                summary = summary.replaceAll("(?i)" + token, " ")
                madeChange = true
            }
        }
        ["FW:", "FWD:", "RE:", "not urgent", "Semi-Urgent", "URGENT", "!", "=", "-", ":", "*", "."].each { String token ->
            if (summary != summary.trim()) {
                summary = summary.trim()
                madeChange = true
            }
            int tokenLength = token.length()
            while (summary != "" && summary.regionMatches(true, 0, token, 0, tokenLength)) {
                summary = summary.substring(tokenLength + 1)
                madeChange = true
            }
            while (summary != "" && summary.regionMatches(true, summary.length() - tokenLength, token, 0, tokenLength)) {
                summary = summary.substring(0, summary.length() - tokenLength)
                madeChange = true
            }
        }
    } catch (Exception ex) {
        summary = ""
    }
    if (summary == "") {
        summary = "Request summary not provided"
        madeChange = false
    }
    if (!madeChange) break
}

if (summary != issue.fields.summary) {
    def result = Unirest.put("/rest/api/2/issue/${issue.key}?notifyUsers=false")
        .header("Content-Type", "application/json")
        .body([ fields: [ summary: summary ], ])
        .asString()
    assert result.status >= 200 && result.status < 300
    logger.info("Updated summary -> ${issue.fields.summary} to ${summary}")
}

logger.trace("Event -> ${issue_event_type_name} - Completed")