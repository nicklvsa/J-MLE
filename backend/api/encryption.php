<?php 

	class EncryptionHelper {

		protected $password;
		public $data;

		function __construct($password) {
			$this->password = $password;
		}

		function getResponse() {
			return $this->data;
		}

		function protect() {
			if($this->password != "") {
				$hashed = password_hash($this->password, PASSWORD_DEFAULT);
				$this->data = array('hashed'=>base64_encode($hashed));
			} else {
				$this->data = array('error'=>'PASSWORD_HASH_EMPTY');
			}
		}

		function verify($thePassword, $compare) {
			if($compare != "" && $thePassword != "") {
				if(password_verify($thePassword, base64_decode($compare))) {
					$this->data = array('status'=>'matching');
				} else {
					$this->data = array('status'=>'hash_error');
				}
			} else {
				$this->data = array('Error'=>'PASSWORD_HASH_EMPTY');
			}
		}
	}

	