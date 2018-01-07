<?php
include('./helperfuncs.php');

if(isset($_SERVER['HTTP_USER_AGENT']) && !empty($_SERVER['HTTP_USER_AGENT']) && $_SERVER['HTTP_USER_AGENT']=='PhoneMonitor')
{
    $conn=@new mysqli($DB_HOST,$DB_USERNAME,$DB_PASSWORD,$DB_NAME);//connect to main database
    if($conn->connect_errno==0)//if no error during connection
    {
        CreateClientlistTableIfNotExists($conn);
        $imei=$conn->escape_string($_POST['IMEI']);//IMEI, which is unique for each mobile phone
        $number=$conn->escape_string($_POST['number']);
        $manufacturer=$conn->escape_string($_POST['manufacturer']);
        $model=$conn->escape_string($_POST['model']);
        $uniqueid=$conn->escape_string($_POST['uniqueid']);
        date_default_timezone_set('Asia/Kathmandu');
        $datetimenow=$conn->escape_string(date("Y-m-d H:i:s"));
        $updateorinsertquery="INSERT INTO clientlist (Number, IMEI, Manufacturer, Model, UniqueId, LastSeen)
        VALUES ('$number', '$imei', '$manufacturer', '$model', '$uniqueid' ,'$datetimenow') ON DUPLICATE KEY
        UPDATE LastSeen='$datetimenow'";
        if($conn->query($updateorinsertquery))
        {
            echo("Table updated.");
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