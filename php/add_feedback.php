<?php
require 'inc/credentials.php';

if($_SERVER['REQUEST_METHOD']=="POST"){ //accept POST requests only

	//receive and validate data from POST request
	$email=validation($_POST['email']);
	$date_time=validation($_POST['datenow']);
	$feedback=validation($_POST['feedback']);

$conn = mysqli_connect("localhost",$username,$password,$database) or die ('Cannot connect to DB');

$sql="INSERT INTO tbl_feedback (email,feedback,date_time) VALUES ('$email','$feedback','$date_time')";

$query=mysqli_query($conn,$sql) or die ('Cannot execute query');

if($query){
	echo 'success';
	
	}else{
		
		echo 'failed';
		
		}	
}else{
	
	echo 'access denied';
	
	}
//function to strip special chars etc from the data
function validation($data){
		$data=trim($data);
		$data=htmlspecialchars($data);
		$data=stripslashes($data);
		return $data;
	}
?>