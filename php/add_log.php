<?php
require 'inc/credentials.php';

if($_SERVER['REQUEST_METHOD']=="POST"){ //accept POST requests only

	//receive and validate data from POST request
	$email=validation($_POST['email']);
	$datenow=validation($_POST['datenow']);
	$coordinates=validation($_POST['coordinates']);
	$serial_number=validation($_POST['serial_number']);
	
	//connection to DB
	$con=mysqli_connect("localhost",$username,$password,$database) or die('Cannot connect to database');
	
	//sql to check / insert record
	$query="SELECT userid FROM tbl_users WHERE email='$email' AND serial_number='$serial_number'";
	$result=mysqli_query($con,$query) or die('Cannot execute query');
	
	
	if(mysqli_num_rows($result)==0){
		//add to user table then log table
		$con_user=mysqli_connect("localhost",$username,$password,$database) or die('Cannot connect to database');
		$insert_user="INSERT INTO tbl_users (email,serial_number) VALUES ('$email','$serial_number') ";
		$result_user=mysqli_query($con_user,$insert_user) or die('Cannot execute user query');
		
		$userid=mysqli_insert_id($con_user);//get userid of newly added user
		
		$insert_log="INSERT INTO tbl_log (userid,coordinates,date_time) VALUES ('$userid','$coordinates','$datenow')";
		mysqli_query($con_user,$insert_log) or die('Cannot execute log query');//insert log
		
		}elseif (mysqli_num_rows($result)==1){
			
			$row=mysqli_fetch_row($result);//get result into single row
			$userid=$row[0];//get the userid returned from the query
			$insert_log="INSERT INTO tbl_log (userid,coordinates,date_time) VALUES ('$userid','$coordinates','$datenow')";
			mysqli_query($con,$insert_log) or die('Cannot execute log query: result 1');//insert log
			}
		echo 'success';
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