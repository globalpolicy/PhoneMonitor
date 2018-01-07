<?php
require_once('./authenticate.php');
include('./helperfuncs.php');

if(isset($_POST['deviceuid']) && !empty($_POST['deviceuid']))
{

    $conn=@new mysqli($DB_HOST,$DB_USERNAME,$DB_PASSWORD,$DB_NAME);
    if($conn->connect_errno==0)
    {
        CreateSettingsTableIfNotExists($conn);
        $deviceuid=$conn->escape_string($_POST['deviceuid']);
        $forceWifiOnForRecordUpload=($_POST['forceWifiOnForRecordUpload']=='true'?1:0);
        $serverTalkInterval=(int)$conn->escape_string($_POST['serverTalkInterval']);

        $updatesettingsquery="REPLACE INTO settings (DeviceUniqueId, ForceWifiOnForRecordUpload, 
        ServerTalkInterval) VALUES ('$deviceuid', $forceWifiOnForRecordUpload, $serverTalkInterval)";
        if($conn->query($updatesettingsquery))
        {
            echo("Settings table updated.");
        }
        else
        {
            echo("Error updating table! $conn->error");
        }
        $conn->close();
    }
    else
    {
        echo("Error connecting to database! $conn->connect_errno");
    } 

}

exit();
?>