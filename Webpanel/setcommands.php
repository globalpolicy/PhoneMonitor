<?php
require_once('./authenticate.php');
include('./helperfuncs.php');

if(isset($_POST['deviceuid']) && !empty($_POST['deviceuid']))
{
    if(isset($_POST['commandid']))
    {   
        $conn=@new mysqli($DB_HOST,$DB_USERNAME,$DB_PASSWORD,$DB_NAME);
        if($conn->connect_errno==0)
        {
            CreateCommandlistTableIfNotExists($conn);
            $deviceuid=$conn->escape_string($_POST['deviceuid']);
            $commandid=$conn->escape_string($_POST['commandid']); 
            $param1=$conn->escape_string(@$_POST['param1']);//without @ to suppress warnings, the echo output will be messed up with warnings
            $param2=$conn->escape_string(@$_POST['param2']);
            $param3=$conn->escape_string(@$_POST['param3']);
            $param4=$conn->escape_string(@$_POST['param4']);
            date_default_timezone_set('Asia/Kathmandu');
            $datetimenow=$conn->escape_string(date("Y-m-d H:i:s"));
            $insertcommandquery="INSERT INTO commandlist (DeviceUniqueId, CommandId, Pending,
            Param1, Param2, Param3, Param4, AddedDateTime) VALUES ('$deviceuid', $commandid, 1,
            '$param1', '$param2', '$param3', '$param4', '$datetimenow')";
            if($conn->query($insertcommandquery))
            {
                echo("Commandlist table updated.");
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
}

exit();
?>