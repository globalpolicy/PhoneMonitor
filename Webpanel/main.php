<?php
include('./helperfuncs.php');
session_start();
if(isset($_SESSION['username']))//if user is already logged in
{
    header('Location:./clientlist.php');
    exit();
}
$message='';
if(isset($_POST['username']) && isset($_POST['password']) && isset($_POST['login']))//if user is logging in just now
{
    $allow=checkUserPassword($_POST['username'],$_POST['password']);
    if($allow=='Ok')
    {
        $_SESSION['username']=$_POST['username'];
        header('Location:./');
        exit();
    }
    else
    {
        $message=$allow;//show this message somewhere appropriate in this page
    }
}
if(isset($_POST['username']) && isset($_POST['password']) && isset($_POST['confirmpassword']) && isset($_POST['register']))//if user is registering just now
{
    if($_POST['password']==$_POST['confirmpassword'])
    {
        $conn=@new mysqli($DB_HOST,$DB_USERNAME,$DB_PASSWORD,$DB_NAME);
        CreateUsersTableIfNotExists($conn);
        if($conn->connect_errno==0)
        {
            $username=$conn->escape_string($_POST['username']);
            $passwordhash=password_hash($_POST['password'],PASSWORD_DEFAULT);
            $createNewUserQuery="INSERT INTO users VALUES ('$username','$passwordhash',0)";
            if($conn->query($createNewUserQuery))
            {
                $message='Account created successfully.';
            }
            else
            {
                $message='Could not create account!';
            }
            
        }
        else
        {
            $message='Could not connect to database.';
        }

    }
    else
    {
        $message='Passwords do not match!';
    }
}


?>

<html>
    <head>
        <meta charset="utf-8"> 
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
        <!-- Latest compiled and minified CSS -->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta.2/css/bootstrap.min.css">

        <!-- jQuery library -->
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>

        <!-- Popper JS -->
        <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.6/umd/popper.min.js"></script>

        <!-- Latest compiled JavaScript -->
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta.2/js/bootstrap.min.js"></script>

        <!-- Font awesome JavaScript -->
        <script src="https://use.fontawesome.com/53d43f5815.js"></script>
        <title>PhoneMonitorCP</title>
    </head>
    <body>

        <nav class="navbar navbar-toggleable-md navbar-dark bg-dark">
            <div class="navbar-brand">PhoneMonitor CP</div>
                <form class="form-inline my-2 my-lg-0 ml-auto" method="POST">
                    <label for="loginUsernameid" style="color:#FF9999;" class="mr-2"><?=$message?></label>
                    <input type="text" class="form-control mr-sm-2" id="loginUsernameid" name="username" placeholder="Username">
                    <input type="password" class="form-control my-2 my-sm-0" id="loginPasswordid" name="password" placeholder="Password">
                    <button type="submit" class="btn btn-outline-success ml-2 my-sm-0" id="loginSubmitbtnid" name="login">Log In</button>
                </form>
            </div>

        </nav>

        <div class="jumbotron jumbotron-fluid" style="height:90%;">
<div class="container-fluid">
            <h3 class="display-5">PhoneMonitor Control Panel</h3>
            <p class="lead">
            Please register an account to use PhoneMonitor Control Panel.<br/>
            Your account will need to be approved by the administrator before you can sign in.
            </p>
            
            <p class="lead">
            Use the form below to register.
            </p>
            <hr class="my-4">
            <div class="col-sm-4" style="background:#B3BAB7;border-radius:20px;">
            <form method="POST">
                <label for="registerUsernameid" class="mt-4">Choose username:</label>
                <input type="text" class="form-control mb-2" id="registerUsernameid" name="username">
                <label for="registerPasswordid">Choose password:</label>
                <input type="password" class="form-control mb-2" id="registerPasswordid" name="password">
                <label for="registerConfirmPasswordid">Confirm password:</label>
                <input type="password" class="form-control mb-2" id="registerConfirmPasswordid" name="confirmpassword">
                <button type="submit" class="btn btn-outline-primary my-3" id="registerSubmitbtnid" name="register">Register</button>
            </form>
            </div>
</div>
        </div>
    </body>
</html>

