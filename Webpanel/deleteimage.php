<?php
require_once('./authenticate.php');
include('./helperfuncs.php');
$image_deviceuid=$_POST['deviceuid'];
$image_timestamp=$_POST['timestamp'];
$image_filename=safeBase64Encode($image_deviceuid).'_'.safeBase64Encode($image_timestamp).'.jpg';
if(file_exists($image_filename) && !is_dir($image_filename))
{
    if(unlink($image_filename))
    {
        echo("Deleted");
    }
}
exit();
?>