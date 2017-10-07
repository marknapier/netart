<html>
<head>
<META http-equiv='Pragma' CONTENT='no-cache'>

<script type="text/javascript">
	<?php 
		// riot_urls.js includes the 'riotUrlHistory' var with the last 10 urls
		$logFileContents = file_get_contents("../riot_urls.js");
		print "$logFileContents;\n";
	?>
</script>

<script>
	function riotURL(u) {
		if (parent.riotToolbar) {
			parent.riotToolbar.riotURL(u);
		}
		return false;
	}

	function appendLink(u) {
		var aTag = document.createElement('a');
		var aTagText = document.createTextNode(u);
		aTag.appendChild(aTagText);
		aTag.setAttribute('href', u);
		aTag.onclick = function () {
			var localu = u; 
			riotURL(localu); 
			return false;
		};
		document.body.appendChild(aTag);
	}

	function appendHistoryLinks() {
		for (var i=0; i < riotUrlHistory.length; i++) {
			appendLink(riotUrlHistory[i]);
			if (i == 2) {
				document.body.appendChild(document.createElement('HR'));
			}
		}
	}
</script>
<style TYPE='text/css'>
	body {
		font-family: Arial,sans-serif; 
		font-size: 9pt; 
		font-weight: normal; 
		color: #303030;
		background-color: #fff;
	}
	a {
		font-family: Arial,sans-serif; 
		font-size: 9pt; 
		font-weight: normal; 
		color: #303030; 
		text-decoration: none; 
		display: block; 
		padding-top: 6px;
		padding-bottom: 6px;
	} 
	a:hover {
		background-color: #4281f4;
		color: #fff;
	}
</style>
</head>

<body>
</body>

<script type="text/javascript">
	appendHistoryLinks();
</script>
</HTML>
