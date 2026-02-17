var match_data, last_match_data, inning_timer, current_batter, wagonXcoOrd, wagonYcoOrd, xPerc, yPerc, canvas, ctx, 
	backgroundImg, lastClickPerc = null, lastSector = null, imgX = 0, imgY = 0, imgW = 300, imgH = 300;
function onWagonPageLoad()
{
  canvas = document.getElementById('wagon_canvas');
  if (!canvas) return;
  ctx = canvas.getContext('2d');

  // Set canvas to fixed drawing size so image drawing math remains correct
  imgW = 300;
  imgH = 300;
  canvas.width = imgW;
  canvas.height = imgH;

  // Remove full-window resize behavior — keep canvas fixed
  window.onresize = null;

/*  current_batter = 'RHB';
  if(last_match_data) {
    last_match_data.match.inning.forEach(function(inns_item,index,arr){
      var sel = document.getElementById('select_match_innings');
      if (sel && sel.value == inns_item.inningNumber) {
        inns_item.battingCard.forEach(function(bat_tm_item,index,arr){
          if(bat_tm_item.status && bat_tm_item.status.toLowerCase() != 'stilltobat' || (bat_tm_item.howOut && bat_tm_item.howOut.trim() != '')) {
            if(bat_tm_item.onStrike && bat_tm_item.onStrike.toLowerCase() == 'yes') {
              if(bat_tm_item.player && bat_tm_item.player.battingStyle) {
                current_batter = bat_tm_item.player.battingStyle.toUpperCase();
              }
            }
          }
        });
      }
    });
  }
  if(current_batter == 'LHB') {
    backgroundImg.src = 'resources/images/wagon_lhb.jpeg';
  } else {
    backgroundImg.src = 'resources/images/wagon_rhb.jpeg';
  }
  */

  backgroundImg = new Image();
  if(match_data != null && match_data.match != null && match_data.match.wagonBatsmanStyle != null
		&& match_data.match.wagonBatsmanStyle.toUpperCase() == 'LHB') {
    backgroundImg.src = 'resources/images/wagon_lhb.jpeg';
  } else {
    backgroundImg.src = 'resources/images/wagon_rhb.jpeg';
  }

  lastClickPerc = null;
  lastSector = null;

  backgroundImg.onload = function() {
    drawBackground();
  };
}
function drawBackground(){
  if (!ctx || !canvas) return;
  // clear entire canvas
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  // compute the draw size from canvas itself (keeps code robust)
  var drawW = canvas.width;
  var drawH = canvas.height;

  if (backgroundImg && backgroundImg.complete) {
    // draw the background to fill the canvas
    ctx.drawImage(backgroundImg, 0, 0, drawW, drawH);
  } else {
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, drawW, drawH);
  }

  // if we had a previous click, replay highlight + dot in the new layout
  if (lastClickPerc && lastSector) {
    highlightSector(lastSector);
    var dotX = (lastClickPerc.xPerc / 100) * drawW;
    var dotY = (lastClickPerc.yPerc / 100) * drawH;
    drawDot(dotX, dotY);
  }
}
function handleClick(e) {
  if (!canvas) return;
  var rect = canvas.getBoundingClientRect();
  var clickX = e.clientX - rect.left;
  var clickY = e.clientY - rect.top;

  // Translate CSS pixels -> drawing pixels in case device pixel ratio or CSS scaling exists
  var scaleX = canvas.width / rect.width;
  var scaleY = canvas.height / rect.height;
  var cx = clickX * scaleX;
  var cy = clickY * scaleY;

  // ignore outside-image clicks (bounds are canvas drawing pixels)
  if (cx < 0 || cx > canvas.width || cy < 0 || cy > canvas.height) {
    alert('Clicked outside the ground image');
    return;
  }

  var xPerc = (cx / canvas.width) * 100;
  var yPerc = (cy / canvas.height) * 100;

  var sector = wagonSector(xPerc, yPerc);
  var infoEl = document.getElementById("whichWagonData");
  if(infoEl) infoEl.innerHTML = 'Wagon X and Y CoOrdinates: ' + xPerc.toFixed(0) + ',' + yPerc.toFixed(0) + ',sector=' + sector;
  var wagonDataEl = document.getElementById("wagonData");
  if (wagonDataEl) wagonDataEl.value = wagonSector(xPerc,yPerc) + ',' + xPerc.toFixed(0) + ',' + yPerc.toFixed(0);

  lastClickPerc = { xPerc: xPerc, yPerc: yPerc };
  lastSector = (typeof sector === 'number' ? sector : null);

  drawBackground();
}
function wagonSector(xPerc, yPerc) {
  // center-relative coordinates (-50..+50)
  const dx = xPerc - 50;
  const dy = yPerc - 50;

  // treat small radius as "center" if you want:
  const distPerc = Math.sqrt(dx*dx + dy*dy);
  if (distPerc < 5) return 'center'; // small dead-zone (5% of image half-size)

 let angleDeg = Math.atan2(dy, dx) * 180 / Math.PI;
 if (angleDeg < 0) angleDeg += 360; // normalize to [0,360)
 angleDeg = (angleDeg + 90) % 360; // rotate so 0° is at the top (north)

  // each sector is 60°. sector 1 => [0,60), sector 2 => [60,120), ... sector 6 => [300,360)
  const sector = Math.floor(angleDeg / 60) + 1;
  return sector; // 1..6
}
function highlightSector(sector) {
  if (!ctx || !canvas) return;
  var cx = canvas.width / 2;
  var cy = canvas.height / 2;
  var radius = Math.min(canvas.width, canvas.height) / 2;

  var angleOffsetDeg = -90;
  var startAngle = ((sector - 1) * 60 + angleOffsetDeg) * Math.PI / 180;
  var endAngle   = (sector * 60 + angleOffsetDeg) * Math.PI / 180;

  ctx.save();
  ctx.beginPath();
  ctx.moveTo(cx, cy);
  ctx.arc(cx, cy, radius, startAngle, endAngle, false);
  ctx.closePath();

  ctx.globalAlpha = 0.25;
  ctx.fillStyle = 'yellow';
  ctx.fill();

  ctx.globalAlpha = 1;
  ctx.strokeStyle = 'orange';
  ctx.lineWidth = 2;
  ctx.stroke();
  ctx.restore();
}
function drawDot(x, y) {
  if (!ctx) return;
  var r = Math.max(4, Math.round(Math.min(canvas.width, canvas.height) * 0.02)); // ~2% of size, min 4px
  ctx.save();
  ctx.beginPath();
  ctx.arc(x, y, r, 0, Math.PI * 2);
  ctx.fillStyle = 'black';
  ctx.fill();
  ctx.lineWidth = 2;
  ctx.strokeStyle = 'white';
  ctx.stroke();
  ctx.restore();
}
/*function onWagonPageLoad()
{
  canvas = document.getElementById('wagon_canvas');
  ctx = canvas.getContext('2d');

  // make canvas full-window
  canvas.height = window.innerHeight;
  canvas.width  = window.innerWidth;

  // update on resize and redraw image
  window.onresize = function () {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    drawBackground(); // redraw the image after resize
  };
  
  current_batter = 'RHB';
  if(last_match_data) {
	last_match_data.match.inning.forEach(function(inns_item,index,arr){
		if(document.getElementById('select_match_innings').value == inns_item.inningNumber) {
			inns_item.battingCard.forEach(function(bat_tm_item,index,arr){
				if(bat_tm_item.status.toLowerCase() != 'stilltobat' || bat_tm_item.howOut.trim() != '') {
					if(bat_tm_item.onStrike.toLowerCase() == 'yes') {
						current_batter = bat_tm_item.player.battingStyle.toUpperCase();
					}
				}	
			});
		}	
	});
  }
  backgroundImg = new Image();
  if(current_batter == 'LHB') {
	backgroundImg.src = 'resources/images/wagon_lhb.jpeg';
  } else {
	backgroundImg.src = 'resources/images/wagon_rhb.jpeg';
  }
  lastClickPerc = null; // { xPerc: number, yPerc: number }
  lastSector = null;

  // when loaded, draw it at the desired position/size
  backgroundImg.onload = function() {
    drawBackground();
  };
}
function drawBackground(){
  // clear canvas
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  if (backgroundImg && backgroundImg.complete) {
    ctx.drawImage(backgroundImg, 0, 0, 300, 300);
  }
  // if we had a previous click, replay highlight + dot in the new layout
  if (lastClickPerc && lastSector) {
    highlightSector(lastSector);
    const dotX = (lastClickPerc.xPerc / 100) * 300;
    const dotY = (lastClickPerc.yPerc / 100) * 300;
    drawDot(dotX, dotY);
  }  
}
function handleClick(e) {
  const rect = canvas.getBoundingClientRect(); // CSS pixels
  const clickX = e.clientX - rect.left;
  const clickY = e.clientY - rect.top;
  // ignore outside-image clicks
  if (clickX < 0 || clickX > 300 || clickY < 0 || clickY > 300) {
    alert('Clicked outside the ground image');
    return;
  }
  // convert to percentage relative to the image (0..100)
  const xPerc = (clickX / 300) * 100;
  const yPerc = (clickY / 300) * 100;

  const sector = wagonSector(xPerc, yPerc);
  document.getElementById("whichWagonData").innerHTML = 
	'Wagon X and Y CoOrdinates: ' + xPerc.toFixed(0) + ',' + yPerc.toFixed(0) + ',sector=' + sector;
  document.getElementById("wagonData").value = wagonSector(xPerc,yPerc) + ',' + xPerc.toFixed(0) + ',' + yPerc.toFixed(0);

  // store for persistence across resizes
  lastClickPerc = { xPerc, yPerc };
  lastSector = (typeof sector === 'number' ? sector : null);

  // redraw background and let resizeAndRedraw replay highlight+dot
  drawBackground();
}
function wagonSector(xPerc, yPerc) {
  // center-relative coordinates (-50..+50)
  const dx = xPerc - 50;
  const dy = yPerc - 50;

  // treat small radius as "center" if you want:
  const distPerc = Math.sqrt(dx*dx + dy*dy);
  if (distPerc < 5) return 'center'; // small dead-zone (5% of image half-size)

 let angleDeg = Math.atan2(dy, dx) * 180 / Math.PI;
 if (angleDeg < 0) angleDeg += 360; // normalize to [0,360)
 angleDeg = (angleDeg + 90) % 360; // rotate so 0° is at the top (north)

  // each sector is 60°. sector 1 => [0,60), sector 2 => [60,120), ... sector 6 => [300,360)
  const sector = Math.floor(angleDeg / 60) + 1;
  return sector; // 1..6
}
function highlightSector(sector) {
  const cx = 300 / 2;
  const cy = 300 / 2;
  const radius = Math.min(300, 300) / 2;

  // rotate drawing so sector 1 is at the top (north)
  const angleOffsetDeg = -90;
  const startAngle = ((sector - 1) * 60 + angleOffsetDeg) * Math.PI / 180;
  const endAngle   = (sector * 60 + angleOffsetDeg) * Math.PI / 180;

  ctx.save();
  ctx.beginPath();
  ctx.moveTo(cx, cy);
  ctx.arc(cx, cy, radius, startAngle, endAngle, false);
  ctx.closePath();

  ctx.globalAlpha = 0.25;
  ctx.fillStyle = 'yellow';
  ctx.fill();

  ctx.globalAlpha = 1;
  ctx.strokeStyle = 'orange';
  ctx.lineWidth = 2;
  ctx.stroke();
  ctx.restore();
}
function drawDot(x, y) {
  const r = 6; // radius in CSS pixels
  ctx.save();
  ctx.beginPath();
  ctx.arc(x, y, r, 0, Math.PI * 2);
  ctx.fillStyle = 'black';
  ctx.fill();
  ctx.lineWidth = 2;
  ctx.strokeStyle = 'white'; // white stroke to keep dot visible on any bg
  ctx.stroke();
  ctx.restore();
}*/

/*function draw(e) {
  // get click relative to canvas
  const rect = canvas.getBoundingClientRect();
  const clickX = e.clientX - rect.left;
  const clickY = e.clientY - rect.top;

  // check if click is inside the drawn image area
  if (clickX < 0 || clickX > 0 + 300 || clickY < 0 || clickY > 0 + 300) {
    // outside image - ignore or return null/0
    alert('Click outside ground image');
    return;
  }

  // convert to percentage relative to the image (0..100)
  const xPerc = (clickX / 300) * 100;
  const yPerc = (clickY / 300) * 100;

  const sector = wagonSector(xPerc, yPerc); // your requested signature
  document.getElementById("whichWagonData").innerHTML = 
	'Wagon X and Y CoOrdinates: ' + xPerc + ',' + yPerc + ',sector=' + sector;
	
  document.getElementById("wagonData").value = wagonSector(xPerc,yPerc) + ',' + xPerc + ',' + yPerc;
	  
  // redraw background and optionally show highlight
  drawBackground();
  if (sector && typeof sector === 'number') {
    highlightSector(sector);
  }
}
function highlightSector(sector) {
  // compute center & radius in canvas coords
  const cx = 300 / 2;
  const cy = 300 / 2;
  const radius = Math.min(300, 300) * 0.5; // reach to the image edge

  // sector to angle mapping (same as wagonSector)
  const startAngle = ((sector - 1) * 60) * Math.PI / 180; // radians
  const endAngle   = (sector * 60) * Math.PI / 180;

  ctx.save();
  ctx.beginPath();
  ctx.moveTo(cx, cy);
  ctx.arc(cx, cy, radius, startAngle, endAngle, false);
  ctx.closePath();

  // translucent fill
  ctx.globalAlpha = 0.25;
  ctx.fillStyle = 'yellow';
  ctx.fill();

  // border
  ctx.globalAlpha = 1;
  ctx.strokeStyle = 'orange';
  ctx.lineWidth = 2;
  ctx.stroke();
  ctx.restore();
}
function onWagonPageLoad()
{
	current_batter = document.getElementById("current_batsman_style");
	wagonXcoOrd = document.getElementById("wagonXcoOrd").value;
	wagonYcoOrd = document.getElementById("wagonYcoOrd").value;
	
	canvas = document.getElementById("wagon_canvas");
	canvas.height = window.innerHeight;
	canvas.width = window.innerWidth; 
	  
	window.onresize = function () {
		canvas.width = window.innerWidth;
		canvas.height = window.innerHeight;
	};
	
	ctx = canvas.getContext("2d");
	background = new Image();
  	background.src = 'resources/images/wagon_rhb.jpeg';
    if(current_batter.value.toUpperCase() == 'LHB') {
	  background.src = 'resources/images/wagon_lhb.jpeg';
	}
	background.onload = function() {
		ctx.drawImage(background,0,0,300,300);  
	}; 	
	  
	window.draw = draw;
}
function draw(e) {
	
	wagonXcoOrd = document.getElementById("log_six_distance").value.split(',')[0];
	wagonYcoOrd = document.getElementById("log_six_distance").value.split(',')[1];
	
	ctx.fillStyle = "white";
	ctx.clearRect(0,0,canvas.width,canvas.height);
	ctx.drawImage(background,0,0,300,300);  
	ctx.beginPath();
	ctx.arc(e.clientX - canvas.getBoundingClientRect().left, 
		e.clientY - canvas.getBoundingClientRect().top, 5, 0, 2 * Math.PI);
	ctx.fill();
	
	xPerc = parseInt(100 * ((e.clientX - wagonXcoOrd) / 300));
	yPerc = parseInt(100 * ((e.clientY - wagonYcoOrd) / 300));
	wagonSector(xPerc,yPerc);
	document.getElementById("whichWagonData").innerHTML = 
		'Wagon X and Y CoOrdinates: ' + xPerc + ',' + yPerc + ',sector=' + wagonSector(xPerc,yPerc)
		+ ' ,clientX=' + e.clientX + ' ,clientY=' + e.clientY + ' ,wagonXcoOrd=' + wagonXcoOrd 
		+ ' ,wagonYcoOrd=' + wagonYcoOrd;
	document.getElementById("wagonData").value = wagonSector(xPerc,yPerc) + ',' + xPerc + ',' + yPerc;
}
function wagonSector(xPerc,yPerc){
	
	if(parseFloat(xPerc / 100) < 0.53)  {
		if(parseFloat(yPerc /100) < 0.72) {
			return 6;
		} else {
			if (parseFloat((yPerc/100) + (xPerc/100) * 250 / 300) >= 1.13) {
		    	return 4;
		    } else {
		      	return 5;
		    }
		}
	  } else {
		  if(parseFloat(yPerc/100) < 0.72) {
		     return 1;
		  } else {
		      if (parseFloat((yPerc/100) + (1 - (xPerc/100)) * 250 / 300) >= 1.11) {
		      	return 3;
		      } else {
		      	return 2;
		      }
		  }
	  } 	 
}
function loadWagonWheelPage(){
	
  var wagonXcoOrd = document.getElementById("wagonXcoOrd").value;
  var wagonYcoOrd = document.getElementById("wagonYcoOrd").value;
  var xPerc, yPerc;
  var canvas = document.getElementById("wagon_canvas");
  canvas.height = window.innerHeight;
  canvas.width = window.innerWidth; 
  
  window.onresize = function () {
	canvas.width = window.innerWidth;
	canvas.height = window.innerHeight;
  };
  
  var ctx = canvas.getContext("2d");
  var background = new Image();
  if(document.getElementById("current_batsman_style").value.toUpperCase() == 'LHB') {
	  background.src = '<c:url value="/resources/images/wagon_lhb.jpeg"/>';
  } else {
  	  background.src = '<c:url value="/resources/images/wagon_rhb.jpeg"/>';
  }
  background.onload = function() {
	ctx.drawImage(background,0,0,300,300);  
  }; 	
  draw(e);
  
  function draw(e) {
  	 ctx.fillStyle = "white";
  	 ctx.clearRect(0,0,canvas.width,canvas.height);
	 ctx.drawImage(background,0,0,300,300);  
  	 ctx.beginPath();
  	 ctx.arc(e.clientX - canvas.getBoundingClientRect().left, 
  		e.clientY - canvas.getBoundingClientRect().top, 5, 0, 2 * Math.PI);
  	 ctx.fill();
  	 xPerc = parseInt(100 * ((e.clientX - wagonXcoOrd) / 300));
  	 yPerc = parseInt(100 * ((e.clientY - wagonYcoOrd) / 300));
  	 wagonSector(xPerc,yPerc);
     document.getElementById("whichWagonData").innerHTML = 
    	 'Wagon X and Y Coordinates: ' + xPerc + ',' + yPerc + ',sector=' + wagSector;
     document.getElementById("wagonData").value = wagSector + ',' + xPerc + ',' + yPerc;
  	 
  };
  
  window.draw = draw;	
}
*/	 	  
/*function secondsTimeSpanToHMS(s) {
  var h = Math.floor(s / 3600); //Get whole hours
  s -= h * 3600;
  var m = Math.floor(s / 60); //Get remaining minutes
  s -= m * 60;
  return h + ":" + (m < 10 ? '0' + m : m) + ":" + (s < 10 ? '0' + s : s); //zero padding on minutes and seconds
}*/
function secondsTimeSpanToMinutesAndSeconds(seconds) {
  var hours = Math.floor(seconds / 3600); //Get whole hours
  seconds -= hours * 3600;
  var minutes = Math.floor(seconds / 60); //Get remaining minutes
  seconds -= minutes * 60;
  return hours + ":" + (minutes < 10 ? '0' + minutes : minutes) + ":" 
	+ (seconds < 10 ? '0' + seconds : seconds); //zero padding on minutes and seconds
}
/*function processMatchTime() {
	if(match_data) {
		if(match_data.match.clock) {
			if(match_data.match.clock.startOrPause == 'start') {
				match_data.match.matchTotalSeconds = match_data.match.matchTotalSeconds + 1;
			}
		}
		if(document.getElementById('match_time_hdr')) {
			document.getElementById('match_time_hdr').innerHTML = 'Match Time: ' + secondsTimeSpanToHMS(match_data.match.matchTotalSeconds);
		}
	}
}*/
function processVariousProcesses(whatToProcess,valueToProcess)
{
	switch (whatToProcess) {
	case 'PROCESS_INNING_DURATION':
		if(match_data == null || match_data.match == null) {
			return false;
		}
		else if (match_data.match.inning != null && match_data.match.inning.length > 0) 
		{
			match_data.match.inning.forEach(function(inn,index,arr){
				if(inn.isCurrentInning.toLowerCase() == 'yes' && inn.inningStatus.toLowerCase() == 'start') {
					inn.duration = +inn.duration + +valueToProcess;
					match_data.timeStats = inn.duration;
					document.getElementById('match_time_hdr').innerHTML = 'Inn: ' 
						+ secondsTimeSpanToMinutesAndSeconds(inn.duration);
					if(inn.inningStats) {
						
						inn.inningStats.timeSinceLastBoundary = +inn.inningStats.timeSinceLastBoundary + +valueToProcess;
						inn.inningStats.timeSinceLastRun = +inn.inningStats.timeSinceLastRun + +valueToProcess;
						inn.inningStats.timeSinceLastRunOffBat = +inn.inningStats.timeSinceLastRunOffBat + +valueToProcess;
						
						match_data.timeStats = match_data.timeStats + ',' + inn.inningStats.timeSinceLastBoundary
							+ ',' + inn.inningStats.timeSinceLastRun + ',' + inn.inningStats.timeSinceLastRunOffBat; 
						
						document.getElementById('match_time_hdr').innerHTML = 
							document.getElementById('match_time_hdr').innerHTML +
							'| Lst Bndry: ' + secondsTimeSpanToMinutesAndSeconds(inn.inningStats.timeSinceLastBoundary) +
							'| Lst Rns: ' + secondsTimeSpanToMinutesAndSeconds(inn.inningStats.timeSinceLastRun) +
							'| Lst Bt Rns: ' + secondsTimeSpanToMinutesAndSeconds(inn.inningStats.timeSinceLastRunOffBat);
							
						if(inn.battingCard != null && inn.battingCard.length > 0) 
						{
							inn.battingCard.forEach(function(bc,bc_index,bc_arr)
							{
								if(bc.batsmanInningStarted != null && bc.batsmanInningStarted.toLowerCase() == 'yes'
									&& bc.status.toLowerCase() == 'not out') 
								{
									bc.duration = bc.duration + parseInt(valueToProcess);
									match_data.timeStats = match_data.timeStats + ',' + bc.playerId + '_' + bc.duration;
								}
							});
						}
					}
				}
			});
		}
		break;
	}
}
function loadingPageProcess(whatToProcess, messageToDisplay)
{
	switch(whatToProcess){
	case 'SHOW':
	    if (messageToDisplay) {
		  document.querySelector('#loadingModal .loader-text').textContent = messageToDisplay;
	    }
	    $('#loadingModal').modal({backdrop: 'static', keyboard: false});
		break;
	case 'HIDE':
		$('#loadingModal').modal('hide');
		break;
	}
}
function afterPageLoad(whichPageHasLoaded)
{
	switch (whichPageHasLoaded) {
	case 'SETUP':
		$('#homeTeamId').select2();
		$('#awayTeamId').select2();
		addItemsToList('LOAD_TOSS',null);
		break;
	case 'MATCH':
   		document.getElementById('match_sub_menu').className = 'panel-collapse collapse show'; // By default show match panel
		break;
	}
}
function initialiseForm(whatToProcess, dataToProcess)
{
	switch (whatToProcess) {
	case 'LOAD_MATCH_PAGE':
		
   		document.getElementById('wagon_sub_menu').className = 'panel-collapse collapse hide';
		document.getElementById('wagon-panel').style.display = 'none';
		document.getElementById('shots_sub_menu').className = 'panel-collapse collapse hide';
		document.getElementById('shots-panel').style.display = 'none';
		document.getElementById('match-panel').style.display = '';
		document.getElementById('match_sub_menu').className = 'panel-collapse collapse show';
		break;
		
	case 'LOAD_SHOTS_PAGE':
		
   		document.getElementById('match_sub_menu').className = 'panel-collapse collapse hide';
		document.getElementById('match-panel').style.display = 'none';
   		document.getElementById('wagon_sub_menu').className = 'panel-collapse collapse hide';
		document.getElementById('wagon-panel').style.display = 'none';
		document.getElementById('shots-panel').style.display = '';
   		document.getElementById('shots_sub_menu').className = 'panel-collapse collapse show';
		break;
		
	case 'LOAD_WAGON_PAGE':

		document.getElementById('wagonData').value = '';
		document.getElementById('log_six_distance').value = '';
		document.getElementById('selectBoundaryHeight').selectedIndex = 0;
		/*document.getElementById('wagonXcoOrd').value = match_data.setup.wagonXOffSet;
		document.getElementById('wagonYcoOrd').value = match_data.setup.wagonYOffSet;
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
				inn.battingCard.forEach(function(bc,index,arr){
					if(match_data.eventFile.events.length > 0) {
						if(bc.playerId == match_data.eventFile.events[match_data.eventFile.events.length-1].eventBatterNo) {
							document.getElementById('current_batsman_style').value = bc.player.battingStyle;
						}
					}
				});
			}
		});*/

   		document.getElementById('match_sub_menu').className = 'panel-collapse collapse hide';
		document.getElementById('match-panel').style.display = 'none';
		document.getElementById('shots_sub_menu').className = 'panel-collapse collapse hide';
		document.getElementById('shots-panel').style.display = 'none';
		document.getElementById('wagon-panel').style.display = '';
   		document.getElementById('wagon_sub_menu').className = 'panel-collapse collapse show';
		
		onWagonPageLoad();
				
		break;
		
	case 'SETUP':
		
		if(dataToProcess) {
			document.getElementById('specialMatchRules').value = dataToProcess.setup.specialMatchRules;
			document.getElementById('matchFileName').value = dataToProcess.match.matchFileName;
			document.getElementById('tournament').value = dataToProcess.setup.tournament;
			document.getElementById('matchIdent').value = dataToProcess.setup.matchIdent;
			document.getElementById('matchType').value = dataToProcess.setup.matchType;
			document.getElementById('groundId').value = dataToProcess.setup.groundId;
			document.getElementById('seasonId').value = dataToProcess.setup.seasonId;
			document.getElementById('homeSubstitutesNumber').value = dataToProcess.setup.homeSubstitutesNumber;
			document.getElementById('awaySubstitutesNumber').value = dataToProcess.setup.awaySubstitutesNumber;
/*			document.getElementById('wagonXOffSet').value = dataToProcess.setup.wagonXOffSet;
			document.getElementById('wagonYOffSet').value = dataToProcess.setup.wagonYOffSet;*/
			document.getElementById('targetRuns').value = dataToProcess.setup.targetRuns;
			document.getElementById('targetType').value = dataToProcess.setup.targetType;
			document.getElementById('targetOvers').value = dataToProcess.setup.targetOvers;
			document.getElementById('reducedOvers').value = dataToProcess.setup.reducedOvers;
			document.getElementById('secondaryTargetRuns').value = dataToProcess.setup.secondaryTargetRuns;
			document.getElementById('secondaryTargetOvers').value = dataToProcess.setup.secondaryTargetOvers;
			document.getElementById('followOnThreshold').value = dataToProcess.setup.followOnThreshold;
			document.getElementById('reviewsPerTeam').value = dataToProcess.setup.reviewsPerTeam;
			document.getElementById('playerGender').value = dataToProcess.setup.playerGender;
			document.getElementById('generateInteractiveFile').value = dataToProcess.setup.generateInteractiveFile;
			document.getElementById('ballsPerOver').value = dataToProcess.setup.ballsPerOver;
			document.getElementById('noBallsRuns').value = dataToProcess.setup.noBallsRuns;
			document.getElementById('speedFilePath').value = dataToProcess.setup.speedFilePath;
			document.getElementById('followOn').value = dataToProcess.setup.followOn;
			/*document.getElementById('numberOfPowerplays').value = dataToProcess.setup.numberOfPowerplays;
			dataToProcess.match.inning.forEach(function(inn,index,arr){
				if(inn.inningNumber == 1) {
					document.getElementById('firstInningFirstPowerplayStartOver').value = inn.firstPowerplayStartOver;
					document.getElementById('firstInningFirstPowerplayEndOver').value = inn.firstPowerplayEndOver;
					document.getElementById('firstInningSecondPowerplayStartOver').value = inn.secondPowerplayStartOver;
					document.getElementById('firstInningSecondPowerplayEndOver').value = inn.secondPowerplayEndOver;
					document.getElementById('firstInningThirdPowerplayStartOver').value = inn.thirdPowerplayStartOver;
					document.getElementById('firstInningThirdPowerplayEndOver').value = inn.thirdPowerplayEndOver;
				} else if (inn.inningNumber == 2) {
					document.getElementById('secondInningFirstPowerplayStartOver').value = inn.firstPowerplayStartOver;
					document.getElementById('secondInningFirstPowerplayEndOver').value = inn.firstPowerplayEndOver;
					document.getElementById('secondInningSecondPowerplayStartOver').value = inn.secondPowerplayStartOver;
					document.getElementById('secondInningSecondPowerplayEndOver').value = inn.secondPowerplayEndOver;
					document.getElementById('secondInningThirdPowerplayStartOver').value = inn.thirdPowerplayStartOver;
					document.getElementById('secondInningThirdPowerplayEndOver').value = inn.thirdPowerplayEndOver;
				}
			});*/
			document.getElementById('homeTeamId').value = dataToProcess.setup.homeTeamId;
			document.getElementById('awayTeamId').value = dataToProcess.setup.awayTeamId;
			//document.getElementById('overs_remaining_select_day').selectedIndex = 0;
			addItemsToList('LOAD_TOSS',dataToProcess);
			addItemsToList('LOAD_TEAMS',dataToProcess);
			document.getElementById('save_match_div').style.display = '';
		} else {
			document.getElementById('specialMatchRules').value = '';
			document.getElementById('matchFileName').value = '';
			document.getElementById('tournament').value = '';
			document.getElementById('matchIdent').value = '';
			document.getElementById('playerGender').selectedIndex = 0;
			document.getElementById('generateInteractiveFile').selectedIndex = 0;
			document.getElementById('ballsPerOver').selectedIndex = 0;
			document.getElementById('noBallsRuns').selectedIndex = 0;
			document.getElementById('speedFilePath').value = '';
			document.getElementById('matchType').selectedIndex = 0;
			document.getElementById('groundId').selectedIndex = 0;
			document.getElementById('seasonId').selectedIndex = 0;
			document.getElementById('homeSubstitutesNumber').selectedIndex = 0;
			document.getElementById('awaySubstitutesNumber').selectedIndex = 0;
/*			document.getElementById('wagonXOffSet').value = '';
			document.getElementById('wagonYOffSet').value = '';*/
			document.getElementById('targetRuns').value = '';
			document.getElementById('targetOvers').value = '';
			document.getElementById('reducedOvers').value = '';
			document.getElementById('targetType').selectedIndex = 0;
			document.getElementById('secondaryTargetRuns').value = '';
			document.getElementById('secondaryTargetOvers').value = '';
			document.getElementById('followOnThreshold').value = '';
			document.getElementById('reviewsPerTeam').selectedIndex = 0;
			document.getElementById('followOn').selectedIndex = 0;
			/*document.getElementById('numberOfPowerplays').selectedIndex = 0;
			document.getElementById('firstInningFirstPowerplayStartOver').value = '1';
			document.getElementById('firstInningFirstPowerplayEndOver').value = '10';
			document.getElementById('firstInningSecondPowerplayStartOver').value = '11';
			document.getElementById('firstInningSecondPowerplayEndOver').value = '40';
			document.getElementById('firstInningThirdPowerplayStartOver').value = '41';
			document.getElementById('firstInningThirdPowerplayEndOver').value = '50';
			document.getElementById('secondInningFirstPowerplayStartOver').value = '1';
			document.getElementById('secondInningFirstPowerplayEndOver').value = '10';
			document.getElementById('secondInningSecondPowerplayStartOver').value = '11';
			document.getElementById('secondInningSecondPowerplayEndOver').value = '40';
			document.getElementById('secondInningThirdPowerplayStartOver').value = '41';
			document.getElementById('secondInningThirdPowerplayEndOver').value = '50';*/
			document.getElementById('homeTeamId').selectedIndex = 0;
			document.getElementById('awayTeamId').selectedIndex = 1;
			//document.getElementById('overs_remaining_select_day').selectedIndex = 0;
			addItemsToList('LOAD_TOSS',null);
			addItemsToList('LOAD_TEAMS',null);
			document.getElementById('save_match_div').style.display = 'none';
		}
		//processUserSelection($('#overs_remaining_select_day'));
		//processUserSelection($('#numberOfPowerplays'));
		processUserSelection($('#matchType'));
		$('#homeTeamId').prop('selectedIndex', document.getElementById('homeTeamId').options.selectedIndex).change();
		$('#awayTeamId').prop('selectedIndex', document.getElementById('awayTeamId').options.selectedIndex).change();
		break;
	}
}
function uploadFormDataToSessionObjects(whatToProcess)
{
	var formData = {};
	var url_path;
	
	switch(whatToProcess.toUpperCase()) {
	case 'UPLOAD_WAGON_DATA':
		url_path = 'upload_wagon_data';
		break;
	case 'UPLOAD_SHOT_DATA':
		url_path = 'upload_shot_data';
		break;
	case 'RESET_MATCH':
		url_path = 'reset_and_upload_match_setup_data';
		break;
	case 'SAVE_MATCH':
		url_path = 'upload_match_setup_data';
		break;
	}
	
	switch (whatToProcess.toUpperCase()) {
    case 'UPLOAD_WAGON_DATA':
		formData.wagonData =
		    ($('#wagonData').val() || '') + ',' +
		    ($('#selectBoundaryHeight').val() || '') + ',' +
		    ($('#log_six_distance').val() || '');
        break;
    case 'UPLOAD_SHOT_DATA':
        formData.shotData =
            $('input.aerial_ground_single_check_only:checked').attr('id') || '';
        break;
    default:
        $('input:not([type="file"]), select, textarea').each(function () {
            if (!this.id) return;
            formData[this.id] = $(this).val() || '';
        });
        break;
	}
	
/*	switch(whatToProcess.toUpperCase()) {
	case 'UPLOAD_WAGON_DATA':
		formData['wagonData'] =
		    $('#wagonData').val() + ',' +
		    $('#selectBoundaryHeight option:selected').val() + ',' +
		    $('#log_six_distance').val();
		formData.append('wagonData',$('#wagonData').val() + ',' + $('#selectBoundaryHeight option:selected').val() 
			+ ',' + $('#log_six_distance').val());  
		break;
	case 'UPLOAD_SHOT_DATA':
		formData.append('shotData', $('input[class=aerial_ground_single_check_only]:checked').attr('id'));  
		break;
	default:
		$('input, select, textarea').each(
			function(index){  
				if ($(this).is("select")) {
				    formData[$(this).attr('id')] = $('#' + $(this).attr('id') + ' option:selected').val();
				} else {
				    formData[$(this).attr('id')] = $(this).val();
				}
				if($(this).is("select")) {
					formData.append($(this).attr('id'),$('#' + $(this).attr('id') + ' option:selected').val());  
				} else {
					formData.append($(this).attr('id'),$(this).val());  
				}	
			}
		);
		break;
	}*/

	$.ajax({    
		headers: {'X-CSRF-TOKEN': $('meta[name="_csrf"]').attr('content')},
        url : url_path,     
        data : formData,
        cache: false,
        //contentType: false,
        //processData: false,
        type: 'POST',     
        success : function(data) {

        	switch(whatToProcess.toUpperCase()) {
        	case 'RESET_MATCH':
        		alert('Match has been reset');
        		break;
			case 'UPLOAD_WAGON_DATA':
				initialiseForm('LOAD_MATCH_PAGE',data);
				if($('#select_wagon_shot option:selected').val() == 'wagon_shots') {
					initialiseForm('LOAD_SHOTS_PAGE',data);
				}
        		break;
			case 'UPLOAD_SHOT_DATA':
				initialiseForm('LOAD_MATCH_PAGE',data);
        		break;
        	case 'SAVE_MATCH':
        		document.setup_form.method = 'post';
        		document.setup_form.action = 'match';
        	   	document.setup_form.submit();
        		break;
        	}
        },    
        error : function(e) {    
       		console.log('Error occured in uploadFormDataToSessionObjects with error description = ' + e);     
        }    
    });	
}
function processUserInput(whatToProcess, valueToProcess){

	switch (whatToProcess) {
	case 'LOG_VARIOUS':
		if(document.getElementById('select_event_div').style.display == 'none') {
			alert('Key press not allowed when match is NOT loaded');
			return false;
		}

		switch (valueToProcess) {
		case 72: case 104:
			addItemsToList('LOAD_HISTORIC', match_data);
			document.getElementById('extra_log_event_row_1').style.display = '';
			//document.getElementById('extra_log_event_row_2').style.display = 'none';
			for(var iRow=0;iRow<=1;iRow++) {
				document.getElementById('load_events_row_' + iRow).style.display = 'none';
			}
			break;
		case 32: // space key
		case 44: // comma
		case 46: // full stop
		case 83: case 115: // s or S
			processCricketProcedures(whatToProcess,valueToProcess);
			break;
		}
		break;
	}	
}

function processUserSelection(whichInput, extraData)
{	
	var select,option,spellNo,total_runs,inn_num;
	
	switch ($(whichInput).attr('name')) {
	case 'overwriteReviewsPerTeam': case 'overwriteSuccessfullReviews': case 'overwriteUnsuccessfullReviews': 
	case 'overwriteRetainedReviews': case 'overwriteUnretainedReviews': case 'overwriteReviewsRemaining':
		
		switch(extraData) {
		case 'SINGLE_CLICK': //Increment
			document.getElementById(whichInput.id).innerHTML = parseInt(document.getElementById(whichInput.id).innerHTML) + 1;
			break;
		case 'DOUBLE_CLICK': // Decrement
			document.getElementById(whichInput.id).innerHTML = parseInt(document.getElementById(whichInput.id).innerHTML) - 1;
			if(parseInt(document.getElementById(whichInput.id).innerHTML) < 0) {
				document.getElementById(whichInput.id).innerHTML = '0';
			}
			break;
		}
		break;
		
	case 'restore_match_btn':
		
		processCricketProcedures('LOAD_BACKUP_MATCH',this);
		break;
	
	case 'load_historic_btn':
		
		if (/^\d+$/.test($('#select_historic_jump_over').val()) == false) {
			alert("Historic over MUST be only numbers");
			$('#select_historic_jump_over').val("");
			return false;
		}		
		match_data.match.inning.forEach(function(inn,index,arr) {
			if($('#select_historic_inning option:selected').val() == inn.inningNumber
				&& $('#select_historic_jump_over').val() > inn.totalOvers) {
				alert('Historic over typed in [' + $('#select_historic_jump_over').val() 
					+ '] is greater than selected inning over number [' + inn.totalOvers + ']');
				return false;
			}
		});			

		processCricketProcedures('JUMP_TO_HISTORIC_POINT',this);
		break;
		
	case 'selectHomePlayersPosition': case 'selectAwayPlayersPosition':
	
		if(current_batter == null) { 
			current_batter = whichInput.id;
			document.getElementById(current_batter).style.border = '2px solid red';
			return;
		}
		if(current_batter == whichInput.id) {
			document.getElementById(current_batter).style.border = '';
			current_batter = null;
			alert('Same batter selected ' + current_batter);
			return;
		}
		if(current_batter.substring(0,4) == whichInput.id.substring(0,4)) {
			
			option = document.getElementById(
				current_batter.substring(0,4) + 'Player_' + current_batter.split("_")[1]).selectedIndex;
			document.getElementById(
				current_batter.substring(0,4) + 'Player_' + current_batter.split("_")[1]).selectedIndex 
				= document.getElementById(whichInput.id.substring(0,4) + 'Player_' 
				+ whichInput.id.split("_")[1]).selectedIndex;
			document.getElementById(
				whichInput.id.substring(0,4) + 'Player_' + whichInput.id.split("_")[1]).selectedIndex = option;	
				
			$("#" + whichInput.id.substring(0,4) + 'Player_' + whichInput.id.split("_")[1]).trigger("change");
			$("#" + current_batter.substring(0,4) + 'Player_' + current_batter.split("_")[1]).trigger("change");
					
			option = document.getElementById(
				current_batter.substring(0,4) + 'CaptainWicketKeeper_' + current_batter.split("_")[1]).selectedIndex;
			document.getElementById(
				current_batter.substring(0,4) + 'CaptainWicketKeeper_' + current_batter.split("_")[1]).selectedIndex 
				= document.getElementById(whichInput.id.substring(0,4) + 'CaptainWicketKeeper_' 
				+ whichInput.id.split("_")[1]).selectedIndex;
			document.getElementById(
				whichInput.id.substring(0,4) + 'CaptainWicketKeeper_' + whichInput.id.split("_")[1]).selectedIndex = option;	
				
			$("#" + whichInput.id.substring(0,4) + 'CaptainWicketKeeper_' + whichInput.id.split("_")[1]).trigger("change");
			$("#" + current_batter.substring(0,4) + 'CaptainWicketKeeper_' + current_batter.split("_")[1]).trigger("change");
			
			document.getElementById(current_batter).style.border = '';
			current_batter = null;
			
		} else {
			
			alert('Different team selected first team = ' + current_batter.substring(0,4) 
				+ ', other team = ' + whichInput.id.substring(0,4) + '. Swap NOT available');
			document.getElementById(current_batter).style.border = '';
			current_batter = null;
	
		}
		break;
	case 'select_pp_inning': case 'select_pp_number':
		addItemsToList('POPULATE_PP_TABLE', match_data);
		break;
		
	case 'log_new_ball_btn':
		processCricketProcedures('LOG_NEW_BALL',this);
		break;
		
	case 'select_impact_team':

		match_data.match.inning.forEach(function(inn,index,arr) {
			if(inn.battingTeamId == $('#select_impact_team option:selected').val()) {
				$('#select_impact_outgoing_player').empty();
				select = document.getElementById('select_impact_outgoing_player');
				if(match_data.setup.homeTeamId == $('#select_impact_team option:selected').val()) {
					match_data.setup.homeSquad.forEach(function(hp,index,arr){
						option = document.createElement('option');
						option.value = hp.playerId;
						option.text = hp.full_name;
						select.appendChild(option);
					});
					if(match_data.setup.homeSubstitutes != null) {
						match_data.setup.homeSubstitutes.forEach(function(hs,bc_index,bc_arr){
							option = document.createElement('option');
							option.value = hs.playerId;
						    option.text = hs.full_name;
						    select.appendChild(option);
						});
					}
					if(match_data.setup.homeOtherSquad != null) {
						match_data.setup.homeOtherSquad.forEach(function(hos,index,arr){
							option = document.createElement('option');
							option.value = hos.playerId;
						    option.text = hos.full_name;
						    select.appendChild(option);
						});
					}
				}else if(match_data.setup.awayTeamId == $('#select_impact_team option:selected').val()) {
					match_data.setup.awaySquad.forEach(function(ap,index,arr){
						option = document.createElement('option');
						option.value = ap.playerId;
						option.text = ap.full_name;
						select.appendChild(option);
					});
					if(match_data.setup.awaySubstitutes != null) {
						match_data.setup.awaySubstitutes.forEach(function(ap,bc_index,bc_arr){
							option = document.createElement('option');
							option.value = ap.playerId;
						    option.text = ap.full_name;
						    select.appendChild(option);
						});
					}
					if(match_data.setup.awayOtherSquad != null) {
						match_data.setup.awayOtherSquad.forEach(function(aos,index,arr){
							option = document.createElement('option');
							option.value = aos.playerId;
						    option.text = aos.full_name;
						    select.appendChild(option);
						});
					}
				}
				removeDuplicateOptions('select_impact_outgoing_player');
/*				inn.battingCard.forEach(function(bc,index,arr){
					option = document.createElement('option');
					option.value = bc.player.playerId;
				    option.text = bc.player.full_name;
				    select.appendChild(option);
				});*/
				
			}
		});			
		$('#select_impact_incoming_player').empty();
		select = document.getElementById('select_impact_incoming_player');
		if(match_data.setup.homeTeamId == $('#select_impact_team option:selected').val()) {
			match_data.setup.homeSquad.forEach(function(hp,index,arr){
				option = document.createElement('option');
				option.value = hp.playerId;
				option.text = hp.full_name;
				select.appendChild(option);
			});
			if(match_data.setup.homeSubstitutes != null) {
				match_data.setup.homeSubstitutes.forEach(function(hs,bc_index,bc_arr){
					option = document.createElement('option');
					option.value = hs.playerId;
				    option.text = hs.full_name;
				    select.appendChild(option);
				});
			}
			if(match_data.setup.homeOtherSquad != null) {
				match_data.setup.homeOtherSquad.forEach(function(hos,index,arr){
					option = document.createElement('option');
					option.value = hos.playerId;
				    option.text = hos.full_name;
				    select.appendChild(option);
				});
			}
		}else if(match_data.setup.awayTeamId == $('#select_impact_team option:selected').val()) {
			match_data.setup.awaySquad.forEach(function(ap,index,arr){
				option = document.createElement('option');
				option.value = ap.playerId;
				option.text = ap.full_name;
				select.appendChild(option);
			});
			if(match_data.setup.awaySubstitutes != null) {
				match_data.setup.awaySubstitutes.forEach(function(ap,bc_index,bc_arr){
					option = document.createElement('option');
					option.value = ap.playerId;
				    option.text = ap.full_name;
				    select.appendChild(option);
				});
			}
			if(match_data.setup.awayOtherSquad != null) {
				match_data.setup.awayOtherSquad.forEach(function(aos,index,arr){
					option = document.createElement('option');
					option.value = aos.playerId;
				    option.text = aos.full_name;
				    select.appendChild(option);
				});
			}
		}
		removeDuplicateOptions('select_impact_incoming_player');
		break;
	
	case 'log_pp_btn':

		processCricketProcedures('LOG_PP_DATA',this);
		break;
		
	case 'log_50_50_over_data_btn':
		
		processCricketProcedures('LOG_50_50_OVER_DATA',this);
		break;
		
	case 'select_change_bowler':
	
		spellNo = 0;
		match_data.match.inning.forEach(function(inn,index,arr) {
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
				if(inn.spells != null) {
					inn.spells.forEach(function(spell,spell_index,spell_arr){
						if(spell.playerId == $('#select_change_bowler option:selected').val() 
							&& spell.spellNumber > spellNo) {
							spellNo = spell.spellNumber;
						}
					});
				}
			}
		});
		$('#select_bowling_spell').empty();
		for(var i=1;i<=10;i++) {
			option = document.createElement('option');
			option.value = i;
			option.text = 'Spell ' + i;
			if(spellNo == i) {
				option.selected = true;
				option.text = option.text + ' (CURRENT)';
			}
		    document.getElementById('select_bowling_spell').appendChild(option);
		}
		break;
	case 'matchDataUpdate':
		processCricketProcedures('LOG_MATCH_DATA_UPDATE',null);
		break;
	case 'upload_shots_btn':
		if($('input:checkbox:checked').length <= 0) {
			alert('At least one checkbox must be ticked');
			return false;
		}
		uploadFormDataToSessionObjects('UPLOAD_SHOT_DATA');
		$('input:checkbox').removeAttr('checked');
		break;
	case 'cancel_shots_btn':
   		document.getElementById('shots_sub_menu').className = 'panel-collapse collapse hide';
		document.getElementById('shots-panel').style.display = 'none';
		document.getElementById('match-panel').style.display = '';
   		document.getElementById('match_sub_menu').className = 'panel-collapse collapse show';
		$('input:checkbox').removeAttr('checked');
		break;
	case 'upload_wagon_btn':
		if(document.getElementById('wagonData').value == '') {
			alert('Wagon data is empty');
			return false;
		}
		if(document.getElementById('log_six_distance').value.replace(/\s/g, "").length <= 0) {
			document.getElementById('log_six_distance').value = '0';
		}
		uploadFormDataToSessionObjects('UPLOAD_WAGON_DATA');
		document.getElementById('wagonData').value = '';
		break;
	case 'cancel_wagon_btn':
		document.getElementById('wagon_sub_menu').className = 'panel-collapse collapse hide';
		document.getElementById('wagon-panel').style.display = 'none';
		document.getElementById('match-panel').style.display = '';
   		document.getElementById('match_sub_menu').className = 'panel-collapse collapse show';
		$('input:checkbox').removeAttr('checked');
		/*
		document.getElementById('wagon_sub_menu').className = 'panel-collapse collapse hide';
		document.getElementById('wagon-panel').style.display = 'none';
		document.getElementById('shots-panel').style.display = '';
   		document.getElementById('shots_sub_menu').className = 'panel-collapse collapse show';
		document.getElementById('wagonData').value = '';*/
		break;
	case 'isDeclared':
		processCricketProcedures('LOG_IS_DECLARED',null);
		break;
	case 'matchType':
		if($('#matchType option:selected').val() == 'TEST' || $('#matchType option:selected').val() == 'FC') {
			document.getElementById('overs_remaining_div').style.display = '';
		} else {
			document.getElementById('overs_remaining_div').style.display = 'none';
		}
/*		switch($('#matchType option:selected').val()) {
		case 'ODI': case 'OD':
			document.getElementById('overs_remaining_select_overs').value = 50;
			break;
		case 'IT20': case 'DT20':
			document.getElementById('overs_remaining_select_overs').value = 20;
			break;
		case 'D10':
			document.getElementById('overs_remaining_select_overs').value = 10;
			break;
		case 'SUPER_OVER':
			document.getElementById('overs_remaining_select_overs').value = 1;
			break;
		case 'TEST': case 'FC':
			document.getElementById('overs_remaining_select_overs').value = 0;
			break;
		}*/
		break;
/*	case 'overs_remaining_select_day':
		document.getElementById('overs_remaining_select_overs').selectedIndex = 89;
		document.getElementById('new_ball_select_overs').value = '90.0';
		match_data.setup.overRemainingNewBall.forEach(function(ovrRemNewBl,index,arr){
			if($('#overs_remaining_select_day option:selected').val() == (index + 1)) {
				document.getElementById('overs_remaining_select_overs').value = ovrRemNewBl.oversRemaining;
				document.getElementById('new_ball_select_overs').value = ovrRemNewBl.newBallOver;
			}
		});			
		break;*/
/*	case 'save_overs_remaining_btn':
		if(match_data == null || match_data.setup == null) {
			alert('Logging of overs remaining & new ball is NOT allowed until the SETUP page is saved');
			return false;
		}
		processCricketProcedures('LOG_OVERS_REMAINING',null);
		break;*/
	case 'log_day_session_btn':
		if($('#select_day option:selected').val() > 0 && $('#select_session option:selected').val() > 0) {
			processCricketProcedures('LOG_DAY_SESSION',null);
		} else {
			alert('Invalid day/session selected.');
			return false;			
		}
		break;
	case 'overwrite_review_btn':
		if (confirm('The REVIEW data of this inning will be overwritten with the data displayed. ' 
			+ 'Are you sure, you want to OVERWRITE review data?') == false) {
			return false;
		}
		processCricketProcedures('LOG_OVERWRITE_REVIEW',null);
		break;
	case 'log_review_btn':
		processCricketProcedures('LOG_REVIEW',null);
		break;
	case 'log_undo_impact_btn':
		processCricketProcedures('LOG_UNDO_IMPACT',null);
		break;
	case 'log_impact_btn':
		for (var i = match_data.eventFile.events.length-1; i >= 0; i--) {
			if(match_data.eventFile.events[i].eventType.toUpperCase() == 'LOG_IMPACT'
				&& match_data.eventFile.events[i].eventInningNumber == $('#select_impact_inning option:selected').val()
				&& match_data.eventFile.events[i].eventBatterNo == $('#select_impact_incoming_player option:selected').val()
				&& match_data.eventFile.events[i].eventOtherBatterNo == $('#select_impact_outgoing_player option:selected').val()) 
			{
				alert('This impact log already exists. [Outgoing player: ' 
					+ $('#select_impact_outgoing_player option:selected').text() + ' & Incoming player: '
					+ $('#select_impact_incoming_player option:selected').text() + ' in inning: ' + $('#select_impact_inning option:selected').text() + ']');
				return false;
			}
		}
		processCricketProcedures('LOG_IMPACT',null);
		break;
	case 'log_finish_btn':
		processCricketProcedures('LOG_FINISH',null);
		break;
	case 'numberOfPowerplays':
		document.getElementById('first_inn_pp_1').style.display = 'none';
		document.getElementById('first_inn_pp_2').style.display = 'none';
		document.getElementById('first_inn_pp_3').style.display = 'none';
		document.getElementById('second_inn_pp_1').style.display = 'none';
		document.getElementById('second_inn_pp_2').style.display = 'none';
		document.getElementById('second_inn_pp_3').style.display = 'none';
		if ($(whichInput).val() >= 1) {
			document.getElementById('first_inn_pp_1').style.display = '';
			document.getElementById('second_inn_pp_1').style.display = '';
		}
		if ($(whichInput).val() >= 2) {
			document.getElementById('first_inn_pp_2').style.display = '';
			document.getElementById('second_inn_pp_2').style.display = '';
		}
		if ($(whichInput).val() >= 3) {
			document.getElementById('first_inn_pp_3').style.display = '';
			document.getElementById('second_inn_pp_3').style.display = '';
		}
		break;
	case 'log_teams_total_overwrite_btn': case 'log_teams_extras_overwrite_btn': //case 'log_substitution_overwrite_btn':
	case 'log_batsman_stats_overwrite_btn': case 'log_bowler_figures_overwrite_btn': case 'log_batsman_howout_overwrite_btn': 
	case 'log_partnerships_overwrite_btn': case 'log_battingcard_overwrite_btn':
		switch ($(whichInput).attr('name')) {
		case 'log_battingcard_overwrite_btn':
			processCricketProcedures('LOG_OVERWRITE_BATTINGCARD',whichInput);
			break;
/*		case 'log_substitution_overwrite_btn':
			processCricketProcedures('LOG_OVERWRITE_SUBSTITUTION',whichInput);
			break;*/
		case 'log_bowler_figures_overwrite_btn':
			processCricketProcedures('LOG_OVERWRITE_BOWLER_FIGURES',whichInput);
			break;
		case 'log_batsman_stats_overwrite_btn':
			processCricketProcedures('LOG_OVERWRITE_BATSMAN_STATS',whichInput);
			break;
		case 'log_teams_total_overwrite_btn': 
			processCricketProcedures('LOG_OVERWRITE_TEAM_TOTAL',whichInput);
			break;
		case 'log_teams_extras_overwrite_btn':
			processCricketProcedures('LOG_OVERWRITE_TEAM_EXTRAS',whichInput);
			break;
		case 'log_batsman_howout_overwrite_btn':
			processCricketProcedures('LOG_OVERWRITE_BATSMAN_HOWOUT',whichInput);
			break;
		case 'log_partnerships_overwrite_btn':
			processCricketProcedures('LOG_OVERWRITE_PARTNERSHIPS',whichInput);
			break;
		}
		break;
	case 'number_of_undo_txt':
		if(whichInput.value < 0 && whichInput.value > match_data.eventFile.events.length) {
			alert('Number of undos is invalid.\r\n Must be a positive number and less than the number of events available [' 
				+ match_data.eventFile.events.length + ']');
			whichInput.selected = true;
			return false;
		}
		break;
	case 'select_existing_cricket_matches':
		if(whichInput.value.toLowerCase().includes('new_match')) {
			initialiseForm('SETUP',null);
		} else {
			processCricketProcedures('LOAD_SETUP',$('#select_existing_cricket_matches option:selected'));
		}
		break;
	case 'log_undo_btn':
		if(match_data.eventFile.events.length > 0) {
			if($('#number_of_undo_txt').val() > match_data.eventFile.events.length) {
				if(confirm('Number of undo [' + $('#number_of_undo_txt').val() + '] is bigger than number of events [' 
						+ match_data.eventFile.events.length + ']. We will make both of them similiar') == false) {
					return false;
				}
				$('#number_of_undo_txt').val(match_data.eventFile.events.length);
			}
			processCricketProcedures('UNDO',$('#number_of_undo_txt'));
		} else {
			alert('No events found');
		}
		break;
	case 'cancel_match_setup_btn':
	    window.location.href = 'match';
	    break;
/*	case 'cancel_match_setup_btn':
		document.setup_form.method = 'post';
		document.setup_form.action = 'match';
	   	document.setup_form.submit();
		break;*/
	case 'matchFileName':
		if(!document.getElementById('matchFileName').value.toLowerCase().includes('.json')) {
			document.getElementById('matchFileName').value = 
				document.getElementById('matchFileName').value + '.json';
		}
		break;
	case 'save_match_btn': case 'reset_match_btn':
		switch ($(whichInput).attr('name')) {
		case 'reset_match_btn':
	    	if (confirm('The setup selections of this match will be retained ' +
	    			'but the match data will be deleted permanently. Are you sure, you want to RESET this match?') == false) {
	    		return false;
	    	}
			break;
		}
		if(document.getElementById('matchFileName').value.trim() == '') {
			alert('Match file name CANNOT be blank. Unable to save the match file');
			return false;
		}
/*		if (!checkEmpty(document.getElementById('matchFileName'),'Match Name')) {
			return false;
		} */
		if($('#homeTeamId option:selected').val() == $('#awayTeamId option:selected').val()) {
			alert('Both teams cannot be same. Please choose different home and away team');
			return false;
		}
/*		for(var tm=1;tm<=2;tm++) {
			for(var i=1;i<11;i++) {
				for(var j=i+1;j<=11;j++) {
					if(tm == 1) {
						if(document.getElementById('homePlayer_' + i).selectedIndex == document.getElementById('homePlayer_' + j).selectedIndex) {
							alert(document.getElementById('homePlayer_' + i).options[
								document.getElementById('homePlayer_' + i).selectedIndex].text.toUpperCase() + 
								' selected multiple times for HOME team');
							return false;
						}
					} else {
						if(document.getElementById('awayPlayer_' + i).selectedIndex == document.getElementById('awayPlayer_' + j).selectedIndex) {
							alert(document.getElementById('awayPlayer_' + i).options[
								document.getElementById('awayPlayer_' + i).selectedIndex].text.toUpperCase() + 
								' selected multiple times for AWAY team');
							return false;
						}
					}
				}
			}
		}*/
		switch ($(whichInput).attr('name')) {
		case 'save_match_btn': 
			option = findSelectDuplicates('name','selectHomePlayers');
			if(option == '') {
				option = findSelectDuplicates('name','selectAwayPlayers');
			} else {
				option = option + ', ' + findSelectDuplicates('name','selectAwayPlayers');
			} 
			if(option != '') {
				alert('Duplicate players found: ' + option);
				return false;
			}
			uploadFormDataToSessionObjects('SAVE_MATCH');
			break;
		case 'reset_match_btn':
			uploadFormDataToSessionObjects('RESET_MATCH');
			break;
		}
		break;
	case 'load_default_team_btn':
		if($('#homeTeamId option:selected').val() == $('#awayTeamId option:selected').val()) {
			alert('Both teams cannot be same. Please choose different home and away team');
			return false;
		}
		processCricketProcedures('LOAD_TEAMS',whichInput);
		addItemsToList('LOAD_TOSS',null);
		document.getElementById('save_match_div').style.display = '';
		break;
	case 'load_historic_match_btn': 
		addItemsToList('LOAD_HISTORIC',null);
		document.getElementById('extra_log_event_row_1').style.display = '';
		//document.getElementById('extra_log_event_row_2').style.display = 'none';
		for(var iRow=0;iRow<=1;iRow++) {
			document.getElementById('load_events_row_' + iRow).style.display = 'none';
		}
		break;
	case 'setup_match_btn':
		document.cricket_form.method = 'post';
		document.cricket_form.action = 'setup';
	   	document.cricket_form.submit();
		break;
	case 'select_match_status':
		option = '';
		spellNo = 0;
		if($('#select_match_status option:selected').val() == 'start') {
			match_data.match.inning.forEach(function(inns_item,index,arr){
				if(document.getElementById('select_match_innings').value == inns_item.inningNumber) {
					if(inns_item.bowlingCard != null) {
						inns_item.bowlingCard.forEach(function(bwl_tm_item,index,arr){
							if(bwl_tm_item.status.toLowerCase().includes('current') && bwl_tm_item.balls > 0) {
								spellNo = 0;
								if(inns_item.spells != null) {
									inns_item.spells.forEach(function(spell,spell_index,spell_arr){
										if(spell.playerId == bwl_tm_item.playerId) {
											spellNo = spell.spellNumber;
										}
									});
								}
								if(spellNo > 0) {
									if(confirm(bwl_tm_item.player.ticker_name + ' last spell number was ' + spellNo + '. Do you wish to start a new spell?') == true) {
										option =  parseInt(spellNo + 1) + ',' + bwl_tm_item.playerId;
									}													
								}
							}
						});
					}
				}
			});	
		}
		processCricketProcedures('INNING_STATUS',option);
		break;
	case 'load_match_btn':
		processCricketProcedures('LOAD_MATCH',$('#select_cricket_matches option:selected'));
		break;
	case 'log_result_btn':
		processCricketProcedures('LOG_RESULT',null);
		break;
	case 'log_event_btn':
		if(!whichInput.id.toLowerCase().includes('change_bowler') && !whichInput.id.toLowerCase().includes('new_batsman')
			&& whichInput.id.toLowerCase() != 'undo' && whichInput.id.toLowerCase() != 'overwrite' 
			&& !whichInput.id.toLowerCase().includes('swap_batsman') && !whichInput.id.toLowerCase().includes('finish')
			&& !whichInput.id.toLowerCase().includes('result') && !whichInput.id.toLowerCase().includes('review')
			&& !whichInput.id.toLowerCase().includes('impact') && !whichInput.id.toLowerCase().includes('pp')) 
		{
			if($('#select_match_status option:selected').val().toLowerCase() != 'start') {
				alert('You must start an innings before you can log an event');
				return false;
			}
		}
		if(whichInput.id.toLowerCase() == 'undo') {
			processCricketProcedures('LOAD_UNDO',whichInput);
		} else {
			processCricketProcedures('LOG_EVENT',whichInput);
		}
		break;
	case 'cancel_wicket_btn': case 'cancel_any_ball_btn': case 'cancel_change_bowler_btn': case 'cancel_result_btn': case 'cancel_impact_btn':
	case 'cancel_undo_btn': case 'cancel_overwrite_btn': case 'cancel_review_btn': case 'cancel_finish_btn': case 'cancel_pp_btn': 
	case 'cancel_historic_btn': 
		document.getElementById('extra_log_event_row_1').style.display = 'none';
		setEventsLayoutSingleColumn(false);
		//document.getElementById('extra_log_event_row_2').style.display = 'none';
		for(var iRow=0;iRow<=1;iRow++) {
			document.getElementById('load_events_row_' + iRow).style.display = '';
		}
		switch ($(whichInput).attr('name')) {
		case 'cancel_undo_btn':
   			addItemsToList('LOAD_EVENTS',match_data); // Load new batsman if 10th out batsman was undone
			setEventsLayoutSingleColumn(false);
			break;
		}
		break;
	default:
		switch ($(whichInput).attr('id')) {
		case 'select_50_50_challenge_runs':
			processUserSelection($('#type_50_50_runs_per_over'));
			break;
		case 'type_50_50_runs_per_over':
		
			if(!isNaN($('#type_50_50_runs_per_over').val())) {
				if((parseInt($('#type_50_50_runs_per_over').val()) - parseInt($('#select_50_50_challenge_runs option:selected').val())) >= 0) {
					$('#select_50_50_bonus_extra_runs').val('+' + Math.trunc(parseInt($('#type_50_50_runs_per_over').val()) / 2));
				} else {
					$('#select_50_50_bonus_extra_runs').val('-' + Math.trunc(parseInt($('#type_50_50_runs_per_over').val()) / 2));
				}
			}
			break;
			
		case 'select_50_50_over_number':
	
			inn_num = 1;
			match_data.match.inning.forEach(function(inn,index,arr){
				if(inn.isCurrentInning.toLowerCase() == 'yes') {
					inn_num = inn.inningNumber;
				}
			});
	
			total_runs = 0;
			var over_data_found = false;
			for (var i = match_data.eventFile.events.length-1; i >= 0; i--) {
				if(match_data.eventFile.events[i].eventInningNumber == inn_num 
					&& (parseInt(match_data.eventFile.events[i].eventOverNo + 1) == parseInt($('#select_50_50_over_number option:selected').val())   
					|| (parseInt(match_data.eventFile.events[i].eventOverNo) == parseInt($('#select_50_50_over_number option:selected').val())
					&& parseInt(match_data.eventFile.events[i].eventBallNo) <= 0))) 
				{
					switch (match_data.eventFile.events[i].eventType) {
					case '1' : case '2': case '3': case '4' : case '5': case '6': case '9': case 'WIDE':
					case 'NO_BALL': case 'BYE': case 'LEG_BYE': case 'PENALTY': case 'LOG_WICKET': case 'LOG_ANY_BALL': 
				    	total_runs = total_runs + match_data.eventFile.events[i].eventRuns;
						switch (match_data.eventFile.events[i].eventType) {
						case 'LOG_WICKET': case 'LOG_ANY_BALL': 
					    	total_runs = total_runs + match_data.eventFile.events[i].eventExtraRuns 
								+ match_data.eventFile.events[i].eventSubExtraRuns;
							break;
						}
						over_data_found = true;
						break;
					}
				} else if(match_data.eventFile.events[i].eventType.toUpperCase() == 'END_OVER' && over_data_found == true) {
					i = -1;
				}
			}
			
			$('#type_50_50_runs_per_over').val(total_runs);
			processUserSelection($('#type_50_50_runs_per_over'));
			break;
			
		case 'new_batsman_div': case 'bye_div': case 'leg_bye_div': case 'wide_div': case 'overwrite_div': 

			$('html,body').animate({scrollTop: document.body.scrollHeight},"fast");
			break;
		
		case 'select_overwrite_team_stats_inning':
			
			match_data.match.inning.forEach(function(inn,index,arr){
				if(inn.inningNumber == $('#select_overwrite_team_stats_inning option:selected').val()) {
					if(document.getElementById('overwrite_total_runs')) {
						document.getElementById('overwrite_total_runs').value = inn.totalRuns;
						document.getElementById('overwrite_total_wickets').value = inn.totalWickets;
						document.getElementById('overwrite_total_overs').value = inn.totalOvers;
						document.getElementById('overwrite_total_balls').value = inn.totalBalls;
						document.getElementById('overwrite_total_fours').value = inn.totalFours;
						document.getElementById('overwrite_total_sixes').value = inn.totalSixes;
						if(match_data.setup.specialMatchRules != null && match_data.setup.specialMatchRules == 'ISPL'){
							document.getElementById('overwrite_total_nines').value = inn.totalNines;
							document.getElementById('overwrite_team_special_runs').value = inn.specialRuns;
						}
					} else if(document.getElementById('overwrite_total_extras')) {
						document.getElementById('overwrite_total_wides').value = inn.totalWides;
						document.getElementById('overwrite_total_no_balls').value = inn.totalNoBalls;
						document.getElementById('overwrite_total_byes').value = inn.totalByes;
						document.getElementById('overwrite_total_leg_byes').value = inn.totalLegByes;
						document.getElementById('overwrite_total_penalties').value = inn.totalPenalties;
						document.getElementById('overwrite_total_extras').value = inn.totalExtras;
					}
				}
			});
			
			break;
		
		case 'select_overwrite_batsman_out_inning':

			$('#select_overwrite_batsman_out').empty();

			select = document.getElementById('select_overwrite_batsman_out');
			match_data.match.inning.forEach(function(inn,index,arr){
				if(inn.inningNumber == $('#select_overwrite_batsman_out_inning option:selected').val()) {
					inn.battingCard.forEach(function(bc,bc_index,bc_arr){
						if(bc.batsmanInningStarted.toLowerCase() == 'yes') {
							option = document.createElement('option');
							option.value = bc.player.playerId;
						    option.text = bc.player.ticker_name;
						    if(bc.status.toLowerCase() == 'out') {
							    option.selected = true;
						    }
						    select.appendChild(option);
						}
					});
				}
			});
		    select.setAttribute('onchange','processUserSelection(this);');
			
			processUserSelection($('#select_overwrite_batsman_out'));

			$('#select_overwrite_batsman_concussionPlayerId').empty();
			select = document.getElementById('select_overwrite_batsman_concussionPlayerId');
			match_data.match.inning.forEach(function(inn,index,arr){
				if(inn.inningNumber == $('#select_overwrite_batsman_out_inning option:selected').val()) {
					if(inn.battingTeamId == match_data.setup.homeTeamId) {
						if(match_data.setup.homeOtherSquad != null) {
							match_data.setup.homeOtherSquad.forEach(function(hos,index,arr){
								option = document.createElement('option');
								option.value = hos.playerId;
							    option.text = hos.ticker_name;
							    select.appendChild(option);
							});
						}
					} else if(inn.battingTeamId == match_data.setup.awayTeamId) {
						if(match_data.setup.awayOtherSquad != null) {
							match_data.setup.awayOtherSquad.forEach(function(aos,index,arr){
								option = document.createElement('option');
								option.value = aos.playerId;
							    option.text = aos.ticker_name;
							    select.appendChild(option);
							});
						}
					}
				}
			});
			//$('#' + select.id).select2({dropdownAutoWidth : true});
			
			break;

		case 'select_overwrite_batting_card_inning':

			$('#select_current_battingcard_index').empty();

			select = document.getElementById('select_current_battingcard_index');
			match_data.match.inning.forEach(function(inn,index,arr){
				if(inn.inningNumber == $('#select_overwrite_batting_card_inning option:selected').val()) {
					inn.battingCard.forEach(function(bc,bc_index,bc_arr){
						option = document.createElement('option');
						option.value = bc.player.playerId;
					    option.text = bc.player.ticker_name;
					    if(bc.onStrike.toLowerCase() == 'yes') {
						    option.selected = true;
					    }
					    select.appendChild(option);
					});
				}
			});
			
			processUserSelection($('#select_current_battingcard_index'));
			break;

		case 'select_overwrite_batsman_stats_inning':

			$('#select_overwrite_batsman').empty();

			select = document.getElementById('select_overwrite_batsman');
			match_data.match.inning.forEach(function(inn,index,arr){
				if(inn.inningNumber == $('#select_overwrite_batsman_stats_inning option:selected').val()) {
					inn.battingCard.forEach(function(bc,bc_index,bc_arr){
						if(bc.batsmanInningStarted.toLowerCase() == 'yes') {
							option = document.createElement('option');
							option.value = bc.player.playerId;
						    option.text = bc.player.ticker_name;
						    if(bc.onStrike.toLowerCase() == 'yes') {
							    option.selected = true;
						    }
						    select.appendChild(option);
						}
					});
				}
			});
			select.setAttribute('onchange','processUserSelection(this);');
			
			processUserSelection($('#select_overwrite_batsman'));
			break;
			
		case 'select_overwrite_bowler_inning':

			$('#select_overwrite_bowler').empty();

			select = document.getElementById('select_overwrite_bowler');
			match_data.match.inning.forEach(function(inn,index,arr){
				if(inn.inningNumber == $('#select_overwrite_bowler_inning option:selected').val()) {
				    inn.bowlingCard.forEach(function(bc,bc_index,bc_arr){
						option = document.createElement('option');
						option.value = bc.player.playerId;
					    option.text = bc.player.ticker_name;
			    	    if(bc.status.toLowerCase() == 'currentbowler') {
						    option.selected = true;
					    }
					    select.appendChild(option);
				    });
				}
			});
			select.setAttribute('onchange','processUserSelection(this);');
			
			processUserSelection($('#select_overwrite_bowler'));
			break;
			
		case 'select_overwrite_partnerships_inning':

			$('#select_overwrite_partnerships').empty();

			select = document.getElementById('select_overwrite_partnerships');
			match_data.match.inning.forEach(function(inn,index,arr){
				if(inn.inningNumber == $('#select_overwrite_partnerships_inning option:selected').val()) {
				    inn.partnerships.forEach(function(part,part_index,bc_arr){
						option = document.createElement('option');
						option.value = part.partnershipNumber;
					    option.text = part.firstPlayer.ticker_name + '/' + part.secondPlayer.ticker_name;
					    option.selected = true;
					    select.appendChild(option);
				    });
				}
			});
			select.setAttribute('onchange','processUserSelection(this);');
			
			processUserSelection($('#select_overwrite_partnerships'));
				
			break;
		
		case 'select_overwrite_partnerships':
		
			match_data.match.inning.forEach(function(inn,index,arr){
				if(inn.inningNumber == $('#select_overwrite_partnerships_inning option:selected').val()) {
					if(inn.partnerships != null) {
						inn.partnerships.forEach(function(part,index,arr){
							if(part.partnershipNumber == $('#select_overwrite_partnerships').val()) {
								document.getElementById('overwrite_partnership_first_batter_runs').value = part.firstBatterRuns;
								document.getElementById('overwrite_partnership_second_batter_runs').value = part.secondBatterRuns;
								document.getElementById('overwrite_partnership_first_batter_balls').value = part.firstBatterBalls;
								document.getElementById('overwrite_partnership_second_batter_balls').value = part.secondBatterBalls;
								document.getElementById('overwrite_partnership_total_runs').value = part.totalRuns;
								document.getElementById('overwrite_partnership_total_balls').value = part.totalBalls;
								document.getElementById('overwrite_partnership_total_fours').value = part.totalFours;
								document.getElementById('overwrite_partnership_total_sixes').value = part.totalSixes;
								if(match_data.setup.specialMatchRules != null && match_data.setup.specialMatchRules == 'ISPL'){
									document.getElementById('overwrite_partnership_total_nines').value = part.totalNines;
								}
							}
						});
					}
				}
			});
			break;
		case 'select_overwrite_batsman':
			match_data.match.inning.forEach(function(inn,index,arr){
				if(inn.inningNumber == $('#select_overwrite_batsman_stats_inning option:selected').val()) {
					inn.battingCard.forEach(function(bc,index,arr){
						if(bc.playerId == $('#select_overwrite_batsman').val()) {
							document.getElementById('overwrite_batsman_runs').value = bc.runs;
							document.getElementById('overwrite_batsman_balls').value = bc.balls;
							document.getElementById('overwrite_batsman_fours').value = bc.fours;
							document.getElementById('overwrite_batsman_sixes').value = bc.sixes;
							document.getElementById('overwrite_batsman_on_strike').value = bc.onStrike;
							document.getElementById('overwrite_batsman_minutes').value = bc.duration;
							if(match_data.setup.specialMatchRules != null && match_data.setup.specialMatchRules == 'ISPL'){
								document.getElementById('overwrite_batsman_nines').value = bc.nines;
							}
						}
					});
				}
			});
			break;
		case 'select_overwrite_batsman_out':
			match_data.match.inning.forEach(function(inn,index,arr){
				if(inn.inningNumber == $('#select_overwrite_batsman_out_inning option:selected').val()) {
					inn.battingCard.forEach(function(bc,index,arr){
						if(bc.playerId == $('#select_overwrite_batsman_out option:selected').val()) {
							if(bc.howOut) {
								document.getElementById('select_overwrite_batsman_howout').value = bc.howOut;
							} else {
								document.getElementById('select_overwrite_batsman_howout').selectedIndex = 0;
							}
							if(bc.howOutFielderId) {
								document.getElementById('select_overwrite_batsman_howOutFielderId').value = bc.howOutFielderId;
							} else {
								document.getElementById('select_overwrite_batsman_howOutFielderId').selectedIndex = 0;
							}
							if(bc.howOutBowlerId) {
								document.getElementById('select_overwrite_batsman_howOutBowlerId').value = bc.howOutBowlerId;
							} else {
								document.getElementById('select_overwrite_batsman_howOutBowlerId').selectedIndex = 0;
							}
							if(bc.concussionPlayerId) {
								document.getElementById('select_overwrite_batsman_concussionPlayerId').value = bc.concussionPlayerId;
							} else {
								document.getElementById('select_overwrite_batsman_concussionPlayerId').selectedIndex = 0;
							}
						}
					});
				}
			});
			break;
		case 'select_overwrite_bowler':
			match_data.match.inning.forEach(function(inn,index,arr){
				if(inn.inningNumber == $('#select_overwrite_bowler_inning option:selected').val()) {
					inn.bowlingCard.forEach(function(bc,index,arr){
						if(bc.playerId == $('#select_overwrite_bowler').val()) {
							document.getElementById('overwrite_bowler_overs').value = bc.overs;
							document.getElementById('overwrite_bowler_balls').value = bc.balls;
							document.getElementById('overwrite_bowler_runs').value = bc.runs;
							document.getElementById('overwrite_bowler_wickets').value = bc.wickets;
							document.getElementById('overwrite_bowler_wides').value = bc.wides;
							document.getElementById('overwrite_bowler_no_balls').value = bc.noBalls;
							document.getElementById('overwrite_bowler_dots').value = bc.dots;
							document.getElementById('overwrite_bowler_maidens').value = bc.maidens;
							document.getElementById('select_overwrite_bowler_status').value = bc.status;
						}
					});
				}
			});
			break;
		case 'overwrite_teams_total': case 'overwrite_teams_extras': case 'overwrite_batsman_stats': case 'overwrite_substitution':
		case 'overwrite_bowler_figures': case 'overwrite_batsman_howout': case 'overwrite_partnerships': case 'overwrite_battingcard':
/*			var error_found = false;
			match_data.match.inning.forEach(function(inn,index,arr){
				if(inn.isCurrentInning.toLowerCase() == 'yes') {
					switch ($(whichInput).attr('id')) {
					case 'overwrite_partnerships':
						if(inn.partnerships.length <= 0) {
							alert('No partnerships found. Overwrite not available');
							error_found = true;
						}
						break;
					case 'overwrite_batsman_stats': case 'overwrite_batsman_howout': case 'overwrite_substitution': case 'overwrite_battingcard':
						if(inn.battingCard.length <= 0) {
							alert('Batting card is empty. Overwrite not available');
							error_found = true;
						}
						break;
					case 'overwrite_bowler_figures':
						if(inn.bowlingCard.length <= 0) {
							alert('Bowling card is empty. Overwrite not available');
							error_found = true;
						}
						break;
					}
				}
			});
			if(error_found == false) {*/
				addItemsToList('LOAD_' + $(whichInput).attr('id').toUpperCase(),null);
				document.getElementById('extra_log_event_row_1').style.display = '';
				setEventsLayoutSingleColumn(true);
				//document.getElementById('extra_log_event_row_2').style.display = 'none';
				for(var iRow=0;iRow<=1;iRow++) {
					document.getElementById('load_events_row_' + iRow).style.display = 'none';
				}
/*			}*/
			break;
		}
		break;
	}
	
}
function processCricketProcedures(whatToProcess, whichInput)
{
	var value_to_process = '', ajax_data_to_send = '', prev_over_no = 0, stats_val = 0;

	switch(whatToProcess) {
	case 'LOG_WICKET': case 'LOG_ANY_BALL': case 'LOG_EVENT':
		last_match_data = match_data; 
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
				prev_over_no = inn.totalOvers;
			}
		});
		break;
	}
		
	switch(whatToProcess) {
	case 'JUMP_TO_HISTORIC_POINT':
		value_to_process = $('#select_historic_inning option:selected').val() + ',' + $('#select_historic_jump_over').val();
		break;
	case 'LOG_PP_DATA': 
		value_to_process = $('#select_pp_inning option:selected').val() + ',' + $('#select_pp_number option:selected').val()
			+ ',' + $('#select_pp_start_over_number').val() + ',' + $('#select_pp_end_over_number').val() + ',' + $('#select_number_of_pps').val();
		break;
	case 'LOG_50_50_OVER_DATA':
		value_to_process = $('#select_50_50_over_number option:selected').val() + ',' + $('#select_50_50_challenge_runs option:selected').val() 
			+ ',' + $('#type_50_50_runs_per_over').val() + ',' + $('#select_50_50_bonus_extra_runs').val() 
			+ ',' + $('#select_50_50_batsman_id option:selected').val() + ',' + $('#select_50_50_bowler_id option:selected').val();
		break;
	case 'LOG_MATCH_DATA_UPDATE':
		value_to_process = $('#matchDataUpdate option:selected').val();
		break;
	case 'LOG_RESULT':
		value_to_process = $('#select_winning_team option:selected').val() + ',' + $('#select_winning_margin').val()
			+ ',' + $('#select_runs_wickets option:selected').val();
		if(match_data.setup.matchType.toUpperCase() == 'TEST' || $('#matchType option:selected').val() == 'FC') {
			value_to_process = value_to_process + ',' + $('#select_inning_margin option:selected').val();
		} else {
			value_to_process = value_to_process + ',' + $('#select_dls_vjd option:selected').val();
		}
		break;
	case 'LOG_IS_DECLARED':
		value_to_process = $('#select_match_innings option:selected').val() + ',' + $('#isDeclared option:selected').val();
		break;
/*	case 'LOG_OVERS_REMAINING':
		value_to_process = $('#overs_remaining_select_day option:selected').val() 
			+ ',' + $('#overs_remaining_select_overs option:selected').val()
			+ ',' + $('#new_ball_select_overs').val();
		break;*/
	case 'LOG_DAY_SESSION':
		value_to_process = $('#select_day option:selected').val() + ',' + $('#select_session option:selected').val();
		break;
	case 'LOG_OVERWRITE_REVIEW': 
		value_to_process = $('#overwriteSuccessfullReviews_1').text() + ',' + $('#overwriteSuccessfullReviews_2').text() + ','
			+ $('#overwriteUnsuccessfullReviews_1').text() + ',' + $('#overwriteUnsuccessfullReviews_2').text();
		break;
	case 'LOG_REVIEW':
		value_to_process = $('#select_review_team option:selected').val() + ',' + $('#select_review_result option:selected').val() 
			+ ',' + $('#select_review_retain option:selected').val();
		break;
	case 'LOG_UNDO_IMPACT': 
		value_to_process = $('#select_impact_undo option:selected').val();
		break;
	case 'LOG_IMPACT':
		value_to_process = $('#select_impact_team option:selected').val() + ',' + $('#select_impact_outgoing_player option:selected').val() 
			+ ',' + $('#select_impact_incoming_player option:selected').val() + ',' + $('#select_impact_or_concussion option:selected').val()
			+ ',' + $('#select_impact_inning option:selected').val();
		break;
	case 'LOG_FINISH':
		value_to_process = $('#start_of_play_txt').val().replace(':','_') + ',' + $('#start_of_lunch_txt').val().replace(':','_') 
			+ ',' + $('#end_of_lunch_txt').val().replace(':','_') + ',' + $('#start_of_tea_txt').val().replace(':','_') + ',' 
			+ $('#end_of_tea_txt').val().replace(':','_') + ',' + $('#end_of_play_txt').val().replace(':','_')
			+ ',' + $('#max_overs_txt').val() + ',' + $('#new_ball_overs_txt').val();
		break;
	case 'LOG_OVERWRITE_TEAM_TOTAL': case 'LOG_OVERWRITE_TEAM_EXTRAS': case 'LOG_OVERWRITE_BOWLER_FIGURES': //case 'LOG_OVERWRITE_SUBSTITUTION':
	case 'LOG_OVERWRITE_BATSMAN_STATS': case 'LOG_OVERWRITE_BATSMAN_HOWOUT': case 'LOG_OVERWRITE_PARTNERSHIPS': case 'LOG_OVERWRITE_BATTINGCARD':
		
		switch (whatToProcess) {
		case 'LOG_OVERWRITE_BATTINGCARD':
			if($('#select_current_battingcard_index option:selected').val() 
				== $('#select_overwrite_battingcard_index option:selected').val()) {
				alert('Same batsman selected. Overwrite NOT Available');
				return false;
			}
			value_to_process = $('#select_current_battingcard_index option:selected').val()
				+ ',' + $('#select_overwrite_battingcard_index option:selected').val()
				+ ',' + $('#select_overwrite_battingcard_delete_batter option:selected').val()
				+ ',' + $('#select_overwrite_batting_card_inning option:selected').val();
			break;
/*		case 'LOG_OVERWRITE_SUBSTITUTION':
			value_to_process = $('#select_current_substitution_index option:selected').val() 
				+ ',' + $('#select_overwrite_substitution_index option:selected').val()
				+ ',' + $('#select_overwrite_substitution_position option:selected').val()
				+ ',' + $('#select_overwrite_substitution_reason option:selected').val();
			break;*/
		case 'LOG_OVERWRITE_PARTNERSHIPS': 
			value_to_process = $('#select_overwrite_partnerships').val() + ',' + $('#overwrite_partnership_first_batter_runs').val() 
				+ ',' + $('#overwrite_partnership_second_batter_runs').val() + ',' + $('#overwrite_partnership_first_batter_balls').val() 
				+ ',' + $('#overwrite_partnership_second_batter_balls').val() + ',' + $('#overwrite_partnership_total_runs').val()
				+ ',' + $('#overwrite_partnership_total_balls').val() + ',' + $('#overwrite_partnership_total_fours').val()
				+ ',' + $('#overwrite_partnership_total_sixes').val();
				if(match_data.setup.specialMatchRules != null && match_data.setup.specialMatchRules == 'ISPL'){
					value_to_process = value_to_process + ',' + $('#overwrite_partnership_total_nines').val();
				}
				value_to_process = value_to_process + ',' + $('#select_overwrite_partnerships_inning').val();
			break;
		case 'LOG_OVERWRITE_BOWLER_FIGURES': 
			value_to_process = $('#select_overwrite_bowler').val() + ',' + $('#overwrite_bowler_overs').val() 
				+ ',' + $('#overwrite_bowler_balls').val() + ',' + $('#overwrite_bowler_runs').val() 
				+ ',' + $('#overwrite_bowler_wickets').val() + ',' + $('#overwrite_bowler_wides').val() 
				+ ',' + $('#overwrite_bowler_no_balls').val() + ',' + $('#overwrite_bowler_dots').val() 
				+ ',' + $('#overwrite_bowler_maidens').val() + ',' + $('#select_overwrite_bowler_status').val() 
				+ ',' + $('#select_overwrite_bowlingcard_delete_bowler option:selected').val() + ',' + $('#select_overwrite_bowler_inning').val();
			break;
		case 'LOG_OVERWRITE_BATSMAN_STATS':
			if($('#overwrite_batsman_on_strike').val()) {
				if(($('#overwrite_batsman_on_strike').val().toUpperCase().indexOf('YES') == -1
				 	&& $('#overwrite_batsman_on_strike').val().toUpperCase().indexOf('NO')  == -1)) {
					
					alert('On Strike can either be a BLANK/YES/NO');
					return false;
				}
			}
			value_to_process = $('#select_overwrite_batsman').val() + ',' + $('#overwrite_batsman_runs').val() 
				+ ',' + $('#overwrite_batsman_balls').val() + ',' + $('#overwrite_batsman_fours').val() 
				+ ',' + $('#overwrite_batsman_sixes').val() + ',' + $('#overwrite_batsman_on_strike').val()
				+ ',' + $('#overwrite_batsman_minutes').val();
				if(match_data.setup.specialMatchRules != null && match_data.setup.specialMatchRules == 'ISPL'){
					value_to_process = value_to_process + ',' + $('#overwrite_batsman_nines').val();
				}
				value_to_process = value_to_process + ',' + $('#select_overwrite_batsman_stats_inning').val();
			break;
		case 'LOG_OVERWRITE_BATSMAN_HOWOUT': 
			value_to_process = $('#select_overwrite_batsman_out').val() + ',' + $('#select_overwrite_batsman_howout option:selected').val() + ',' 
				+ $('#select_overwrite_batsman_howOutFielderId').val() + ',' + $('#select_overwrite_batsman_howOutBowlerId').val() 
				+ ',' + $('#select_overwrite_batsman_concussionPlayerId').val() + ',' + $('#select_overwrite_batsman_out_substitute').val() 
				+ ',' + $('#select_overwrite_batsman_out_inning').val();
			break;
		case 'LOG_OVERWRITE_TEAM_TOTAL':
			value_to_process = $('#overwrite_total_runs').val() + ',' + $('#overwrite_total_wickets').val() + ',' + $('#overwrite_total_overs').val()
				+ ',' + $('#overwrite_total_balls').val() + ',' + $('#overwrite_total_fours').val() + ',' + $('#overwrite_total_sixes').val();
			if(match_data.setup.specialMatchRules != null && match_data.setup.specialMatchRules == 'ISPL'){
				value_to_process = value_to_process + ',' + $('#overwrite_team_special_runs').val() + ',' + $('#overwrite_total_nines').val();
			}
			value_to_process = value_to_process + ',' + $('#select_overwrite_team_stats_inning').val();
			break;
		case 'LOG_OVERWRITE_TEAM_EXTRAS':
			value_to_process = $('#overwrite_total_wides').val() + ',' + $('#overwrite_total_no_balls').val() + ',' + $('#overwrite_total_byes').val()
				+ ',' + $('#overwrite_total_leg_byes').val() + ',' + $('#overwrite_total_penalties').val() + ',' + $('#overwrite_total_extras').val()
				+ ',' + $('#select_overwrite_team_stats_inning').val();
			break;
		}
		break;
		
	case 'LOAD_TEAMS':
	
		value_to_process = $('#homeTeamId option:selected').val() + ',' + $('#awayTeamId option:selected').val();
			+ ',' + $('#playerGender option:selected').val();
		break;
	
	case 'LOAD_BACKUP_MATCH':
		
		value_to_process = match_data.match.matchFileName;
		break;
	
	case 'LOAD_MATCH': case 'LOAD_SETUP':
		
		value_to_process = whichInput.val();
		break;
		
	case 'LOG_WICKET': case 'LOG_ANY_BALL': case 'LOG_EVENT': case 'CHANGE_BOWLER': // case 'NEW_BATSMAN':

		var not_out_batsmen_count = 0, cur_bowler_balls = 0;
		var current_bowler_error = '', previous_bowler_repeat_error = '', new_batsman_error = '', error_txt = '';
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
				if(whichInput.id.toLowerCase().includes('result') && inn.inningNumber == 1) {
					error_txt = "Result not available in 1st inning";
				}
				if(inn.bowlingCard != null) {
					inn.bowlingCard.forEach(function(bc,index,arr){
						if(bc.status.toLowerCase() == 'currentbowler') {
							cur_bowler_balls = bc.balls;
							current_bowler_error = bc.player.ticker_name + ' already in. Option NOT available';
						} else if(bc.status.toLowerCase() == 'lastbowler') {
							if(whatToProcess == 'CHANGE_BOWLER') {
								if(bc.playerId == $('#select_change_bowler option:selected').val()) {
									previous_bowler_repeat_error = bc.player.ticker_name + ' has bowled previous over';
									if(bc.overs >= (match_data.setup.maxOvers / 5)){
										previous_bowler_repeat_error = bc.player.ticker_name + ' has bowled maximum overs allowed';
									}
								}
							}
						}
					});
				}
				inn.battingCard.forEach(function(bc,index,arr){
					if(whichInput.id.toLowerCase().includes('new_batsman')) {
						if(bc.playerId == whichInput.id.split(',')[1]) {
							if(bc.status.toLowerCase() == 'not out') {
								new_batsman_error = bc.player.ticker_name + ' is already in';
							} else if(bc.status.toLowerCase() == 'out') {
								new_batsman_error = bc.player.ticker_name + ' is an OUT batsman';
							} else if(bc.status.toLowerCase() != 'stilltobat') {
								new_batsman_error = bc.player.ticker_name + ' is NOT in STILL TO BAT list';
							}
						}						
					}						
					if(bc.status.toLowerCase() == 'not out') {
						not_out_batsmen_count = not_out_batsmen_count + 1;
					} 
				});
			}
		});
		if(!whichInput.id.toLowerCase().includes('change_bowler') && !whichInput.id.toLowerCase().includes('new_batsman')
			&& !whichInput.id.toLowerCase().includes('undo') && !whichInput.id.toLowerCase().includes('review')
			&& !whichInput.id.toLowerCase().includes('result') && !whichInput.id.toLowerCase().includes('50-50')
			&& !whichInput.id.toLowerCase().includes('impact') && !whichInput.id.toLowerCase().includes('pp')
			&& !whichInput.id.toLowerCase().includes('swap_batsman') && !whichInput.id.toLowerCase().includes('finish')
			&& !whichInput.id.toLowerCase().includes('wicket') && !whichInput.id.toLowerCase().includes('any_ball')) 
			{
			if(!whichInput.id.toLowerCase().includes('any_ball')) {
				if(current_bowler_error == '') {
					alert('No current bowler found. Please use CHANGE BOWLER option');
					return false;
				} 
			}
			if(!whichInput.id.toLowerCase().includes('end_over') && !whichInput.id.toLowerCase().includes('result')) {
				if(not_out_batsmen_count != 2) {
					alert('Two batsmen not in. Please use new batsman option');
					return false;
				}
			}
		} else if(whichInput.id.toLowerCase().includes('result')) {
			if(error_txt) {
				alert(error_txt);
				return false;
			} 
		} else if (whichInput.id.toLowerCase().includes('new_batsman')) {
			if(not_out_batsmen_count == 2) {
				alert('Two batsmen already in. Option NOT available');
				return false;
			} else if (new_batsman_error != '') {
				alert(new_batsman_error);
				return false;
			}
		} else if (whichInput.id.toLowerCase().includes('change_bowler') && whatToProcess != 'CHANGE_BOWLER') {
			if(current_bowler_error) {
				alert(current_bowler_error);
				return false;
			} 
		} else if (whatToProcess == 'CHANGE_BOWLER') {
			if(previous_bowler_repeat_error) {
				if(confirm(previous_bowler_repeat_error + '. Do you wish to continue?') == false) {
					return false;
				}
			}
			if(cur_bowler_balls > 0) {
				if(confirm('Current bowler has bowled ' + cur_bowler_balls + ' balls. End this over?') == false) {
					return false;
				}
			}
		}

		if(whichInput.id.toLowerCase() == 'wicket' || whichInput.id.toLowerCase() == 'any_ball' || whichInput.id.toLowerCase() == 'change_bowler' 
			|| whichInput.id.toLowerCase() == 'review' || whichInput.id.toLowerCase() == 'result' || whichInput.id.toLowerCase() == '50-50' 
			|| whichInput.id.toLowerCase() == 'impact' || whichInput.id.toLowerCase() == 'finish' || whichInput.id.toLowerCase() == 'pp') 
		{
			if(whichInput.id.toLowerCase() == 'wicket') {
				addItemsToList('LOAD_HOW_OUT',null);
			} else if(whichInput.id.toLowerCase() == 'any_ball') {
				addItemsToList('LOAD_ANY_BALL',null);
			} else if(whichInput.id.toLowerCase() == 'change_bowler') {
				addItemsToList('LOAD_CHANGE_BOWLER',null);
			} else if(whichInput.id.toLowerCase() == 'review') {
				addItemsToList('LOAD_REVIEW',null);
			} else if(whichInput.id.toLowerCase() == 'result') {
				addItemsToList('LOAD_RESULT',null);
			} else if(whichInput.id.toLowerCase() == '50-50') {
				addItemsToList('LOAD_FIFTY-FIFTY',null);
			} else if(whichInput.id.toLowerCase() == 'pp') {
				addItemsToList('LOAD_PP',null);
			} else if(whichInput.id.toLowerCase() == 'finish') {
				addItemsToList('LOAD_FINISH',null);
			} else if(whichInput.id.toLowerCase() == 'impact') {
				addItemsToList('LOAD_IMPACT',null);
			}  
			document.getElementById('extra_log_event_row_1').style.display = '';
			setEventsLayoutSingleColumn(true);
/*			if(whichInput.id.toLowerCase() == 'any_ball' || whichInput.id.toLowerCase() == 'pp') {
				document.getElementById('extra_log_event_row_2').style.display = '';
			} else {
				document.getElementById('extra_log_event_row_2').style.display = 'none';
			}*/
			for(var iRow=0;iRow<=1;iRow++) {
				document.getElementById('load_events_row_' + iRow).style.display = 'none';
			}
			return false;
		} 
		switch(whatToProcess) {
		case 'LOG_EVENT':
			value_to_process = whichInput.id;
			break;
		case 'CHANGE_BOWLER':
			value_to_process = $('#select_change_bowler option:selected').val() + ',' + $('#select_bowling_end option:selected').val()
				+ ',' + $('#select_bowling_spell option:selected').val();
				//+ ',' + $('#select_outgoing_bowler option:selected').val() + ',' + $('#select_substitute_reason option:selected').val();
			if(match_data.setup.specialMatchRules != null && match_data.setup.specialMatchRules == 'ISPL'){
				value_to_process = value_to_process + ',' + $('#select_bowling_ball_type option:selected').val()
					+ ',' + $('#select_50_50_challenge_runs option:selected').val();
			}
			break;
		case 'LOG_WICKET': 
			value_to_process = $('#select_how_out option:selected').val() + ',' + $('#select_out_batsman option:selected').val() 
				+ ',' + $('#select_out_fielder option:selected').val() + ',' + $('#select_batsman option:selected').val() + ',' 
				+ $('#select_batsman_runs option:selected').val() + ',' + $('#select_concussion_replacement_player option:selected').val()
				+ ',' + $('#select_how_out_fielder_substitute option:selected').val();
			break; 
		case 'LOG_ANY_BALL':
			value_to_process = $('#select_delivery_type option:selected').val() + ',' + $('#select_how_out option:selected').val() + ',' + $('#select_out_batsman option:selected').val() 
				+ ',' + $('#select_out_fielder option:selected').val() + ',' + $('#select_batsman option:selected').val() + ',' + $('#select_batsman_runs option:selected').val() 
				+ ',' + $('#select_runs_type option:selected').val() + ',' + $('#select_extra option:selected').val() + ',' + $('#select_extra_runs option:selected').val()
				+ ',' + $('#select_concussion_replacement_player option:selected').val() + ',' + $('#select_do_not_increment_ball').is(':checked')
				+ ',' + $('#select_how_out_fielder_substitute option:selected').val();
			break; 
		}
		break;
	
	case 'LOG_VARIOUS':
		
		switch(whichInput) {
		case 32: // space key
			value_to_process = 'reload_match';
			break;
		case 44: // comma
			value_to_process = 'bowler_running';
			break;
		case 46: // full stop
			value_to_process = 'ball_release';
			break;
		case 83: case 115: // s or S
			value_to_process = 'update_bowlers_speeds';
			break;
		default:
			return false;
		}
		break;
		
	case 'UNDO':
		
		value_to_process = $('#number_of_undo_txt').val();
		break;
		
	case 'SELECT_INNING': 
		
		value_to_process = whichInput.val();
		break;
    
	case 'INNING_STATUS': 
		
		value_to_process = $('#select_match_innings option:selected').val() + ',' 
			+ $('#select_match_status option:selected').val() ;
		if(whichInput) {
			value_to_process = value_to_process + ',' + whichInput; 
		}
		break;
		
	}

	ajax_data_to_send = 'whatToProcess=' + whatToProcess + '&valueToProcess=' + value_to_process; 
	if(match_data) {
		if(match_data.timeStats) {
			ajax_data_to_send = ajax_data_to_send + '&timeStatsToProcess=' + match_data.timeStats;
		}
	}

	//loadingPageProcess('SHOW', 'Processing...');

	$.ajax({    
        type : 'Get',     
		url: 'processCricketProcedures',     
        data : ajax_data_to_send, 
        dataType : 'json',
        success : function(data) {
        	switch(whatToProcess) {
        	case 'LOAD_UNDO': 
        		break;
			case 'LOG_VARIOUS':
				switch(whichInput) {
				case 32: // space key to refresh
	        		addItemsToList('LOAD-INNINGS',data);
					addItemsToList('LOAD_EVENTS',data);
	        		document.getElementById('select_event_div').style.display = '';
					setEventsLayoutSingleColumn(false);	        		
	        		//document.getElementById('load_historic_match_div').style.display = '';
					break;
				case 83: case 115: // s or S
					alert("This inning's bowler's speeds has been updated");
					break;
				}
				match_data = data;
        		break;
        	default:
            	match_data = data;
        		break;
        	}
        	switch(whatToProcess) {
			case 'LOG_FINISH':
				alert('Finish time added for the match'); 
				break;
/*			case 'LOG_OVERS_REMAINING':
				alert('Over remaining value ' + $('#overs_remaining_select_overs option:selected').val()
					+ ' and new ball over ' + $('#new_ball_select_overs').val() 
					+ ' added for day ' + $('#overs_remaining_select_day option:selected').val());
				break;*/
			case 'LOG_REVIEW': case 'LOG_OVERWRITE_REVIEW':
				addItemsToList('LOAD_MATCH',data);
        		addItemsToList('LOAD_REVIEW',data);
				break;
        	case 'UNDO':
        		addItemsToList('LOAD_MATCH',data);
        		processCricketProcedures('LOAD_UNDO',null);
        		break;
        	case 'LOAD_TEAMS':
        		addItemsToList('LOAD_TEAMS',data);
        		break;
        	case 'LOAD_UNDO': 
        		if(data.eventFile.events == null || data.eventFile.events.length <= 0) {
        			alert('No events found to perform undoes');
					document.getElementById('extra_log_event_row_1').style.display = 'none';
					//document.getElementById('extra_log_event_row_2').style.display = 'none';
	    			for(var iRow=0;iRow<=1;iRow++) {
	    				document.getElementById('load_events_row_' + iRow).style.display = '';
	    			}
        			return false;
        		}
        		addItemsToList('LOAD_UNDO',data);
        		match_data = data;
        		if(document.getElementById('extra_log_event_row_1').style.display = 'none') {
					document.getElementById('extra_log_event_row_1').style.display = '';
					setEventsLayoutSingleColumn(true);
					//document.getElementById('extra_log_event_row_2').style.display = 'none';
	    			for(var iRow=0;iRow<=1;iRow++) {
	    				document.getElementById('load_events_row_' + iRow).style.display = 'none';
	    			}
        		}
        		break;
        	case 'LOG_WICKET': case 'LOG_ANY_BALL':
        		stats_val = $('#select_batsman_runs option:selected').val();
        		addItemsToList('LOAD_MATCH',data);
        		addItemsToList('LOAD_EVENTS',data); // Load new batsman if retired hurt was logged
				document.getElementById('extra_log_event_row_1').style.display = 'none';
				setEventsLayoutSingleColumn(false);
				//document.getElementById('extra_log_event_row_2').style.display = 'none';
    			for(var iRow=0;iRow<=1;iRow++) {
    				document.getElementById('load_events_row_' + iRow).style.display = '';
    			}
				if(($('#select_wagon_shot option:selected').val() == 'wagon' || $('#select_wagon_shot option:selected').val() == 'wagon_shots') && stats_val > 0) {
					initialiseForm('LOAD_WAGON_PAGE',data);
				}
        		break;
    		case 'LOG_OVERWRITE_TEAM_TOTAL': case 'LOG_OVERWRITE_TEAM_EXTRAS': case 'CHANGE_BOWLER': 
			case 'LOG_RESULT': case 'LOG_OVERWRITE_BOWLER_FIGURES': case 'LOG_OVERWRITE_BATSMAN_STATS': case 'LOG_OVERWRITE_BATSMAN_HOWOUT': 
			case 'LOG_OVERWRITE_PARTNERSHIPS': case 'LOG_OVERWRITE_BATTINGCARD': case 'LOG_50_50_OVER_DATA': case 'LOG_IMPACT': 
			case 'LOG_PP_DATA': case 'LOG_UNDO_IMPACT': case 'JUMP_TO_HISTORIC_POINT': // case 'NEW_BATSMAN': case 'LOG_OVERWRITE_SUBSTITUTION':
        		addItemsToList('LOAD_MATCH',data);
	        	switch(whatToProcess) {
	        	case 'LOG_OVERWRITE_BATTINGCARD': case 'JUMP_TO_HISTORIC_POINT':
	        		addItemsToList('LOAD_EVENTS',data); // Load new batsman if retired hurt was logged
	        		break;
	        	}
				document.getElementById('extra_log_event_row_1').style.display = 'none';
				setEventsLayoutSingleColumn(false);
				//document.getElementById('extra_log_event_row_2').style.display = 'none';
    			for(var iRow=0;iRow<=1;iRow++) {
    				document.getElementById('load_events_row_' + iRow).style.display = '';
    			}
        		break;
			case 'LOG_DAY_SESSION': case 'UNDO': case 'START_PAUSE_TIME': case 'LOG_EVENT': case 'LOG_NEW_BALL':
        		addItemsToList('LOAD_MATCH',data);
        		switch(whatToProcess) {
				case 'LOG_NEW_BALL':
					data.match.inning.forEach(function(inn,index,arr){
						if(inn.isCurrentInning.toLowerCase() == 'yes') {
							alert('New ball logged for inning ' + inn.inningNumber 
								+ ' at over ' + inn.newBallOver);
							return false;
						}
					});					
					break;
        		case 'LOG_EVENT':
					if($('#select_wagon_shot option:selected').val()  == 'wagon'
						|| $('#select_wagon_shot option:selected').val() == 'wagon_shots') {
						switch(whichInput.id.toLowerCase()){
						case '0':
							if($('#select_wagon_shot option:selected').val() == 'wagon_shots') {
								initialiseForm('LOAD_SHOTS_PAGE',data);
							}
							break;
						case '1': case '2': case '3': case '4,boundary': case '4,runs': case '5':  
						case '6,boundary': case '6,runs': case '9,boundary': case '9,runs': 
							initialiseForm('LOAD_WAGON_PAGE',data);
							break;
						} 
					}
					break;
				}
        		break;
        	case 'LOAD_MATCH': case 'SELECT_INNING': case 'INNING_STATUS': case 'LOG_IS_DECLARED': case 'LOAD_BACKUP_MATCH':
        		addItemsToList('LOAD-INNINGS',data);
        		addItemsToList('LOAD_EVENTS',data);
        		document.getElementById('select_event_div').style.display = '';
				setEventsLayoutSingleColumn(false);	        		
        		//document.getElementById('load_historic_match_div').style.display = '';
				switch(whatToProcess) {
				case 'INNING_STATUS':
					if($('#select_match_status option:selected').val() == 'start') {
					  	inning_timer = setInterval(function(){processVariousProcesses('PROCESS_INNING_DURATION',1)}, 1000);
					} else if($('#select_match_status option:selected').val() == 'pause') {
						if(data.timeStats) {
							document.getElementById('match_time_hdr').innerHTML = data.timeStats
						}
						clearInterval(inning_timer);
					}
					break;
				case 'LOAD_MATCH':
					if($('#select_wagon_shot option:selected').val() == 'wagon'
						|| $('#select_wagon_shot option:selected').val() == 'wagon_shots') {
						onWagonPageLoad();        		
					}
					break;
				}
        		break;
        	case 'LOAD_SETUP':
        		initialiseForm('SETUP',data);
        		break;
        	}
        	switch(whatToProcess) {
			case 'LOG_WICKET': case 'LOG_ANY_BALL': case 'LOG_EVENT': 
				if(data.eventFile.status) {
	        		document.getElementById('match_error_lbl').innerHTML = data.eventFile.status;
	        		document.getElementById('match_error_lbl').style.color = 'red';
				} else {
	        		document.getElementById('match_error_lbl').innerHTML = '';
	        		document.getElementById('match_error_lbl').style.color = '';
				}
				data.match.inning.forEach(function(inn,index,arr){
					if(inn.isCurrentInning.toLowerCase() == 'yes') {
						if(prev_over_no != inn.totalOvers && whichInput.id != 'end_over') {
							value_to_process = 'End over?';
							if(parseInt(match_data.setup.ballsPerOver) < 6) {
								value_to_process = value_to_process + ' (' 
									+ match_data.setup.ballsPerOver + ' balls per over)';
							}
							if(confirm(value_to_process) == true) {
								processCricketProcedures('LOG_EVENT',document.getElementById('end_over'));
							}
						}
					}
				});
				if(data.match.matchStatus.toLowerCase().includes(' win by ') 
					|| data.match.matchStatus.toLowerCase().includes(' win on ') 
					|| data.match.matchStatus.toLowerCase().includes(' tied')
					|| data.match.matchStatus.toLowerCase().includes(' beat ')) {
					if(confirm('Match finished [' + data.match.matchStatus + ']. Do you wish to END match?') == true) {
						$("#select_match_status").val('pause');
						processCricketProcedures('INNING_STATUS',null);
					}
				}
/*				data.match.inning.forEach(function(inn,index,arr){
					if(inn.isCurrentInning.toLowerCase() == 'yes' && inn.inningNumber >= 2 && data.setup.matchType.toUpperCase() != 'TEST'
						 && data.setup.matchType.toUpperCase() != 'FC') {
						if(data.setup.targetRuns > 0) {
							if(inn.totalRuns >= data.setup.targetRuns) {
								if(confirm('Second innings target [' + data.setup.targetRuns + '] has been reached. End match?') == true) {
									$("#select_match_status").val('pause');
									processCricketProcedures('INNING_STATUS',null);
								}
							} else if(inn.totalOvers >= data.setup.targetOvers) {
								if(confirm('Second innings maximum overs [' + data.setup.targetOvers + '] has been reached. End match?') == true) {
									$("#select_match_status").val('pause');
									processCricketProcedures('INNING_STATUS',null);
								}
							}
						} else {
							if(inn.totalRuns > data.match.inning[0].totalRuns) {
								if(confirm('Second innings target [' + inn.totalRuns + '] has been reached. End match?') == true) {
									$("#select_match_status").val('pause');
									processCricketProcedures('INNING_STATUS',null);
								}
							} else if(data.setup.totalOvers >= inn.totalOvers) {
								if(confirm('Second innings maximum overs [' + data.setup.totalOvers + '] has been reached. End match?') == true) {
									$("#select_match_status").val('pause');
									processCricketProcedures('INNING_STATUS',null);
								}
							}
						}
					}
				});*/
        		break;
        	}
			//loadingPageProcess('HIDE', null);
	    },    
	    error : function(e) {    
	  	 	console.log('Error occured in ' + whatToProcess + ' with error description = ' + e);     
			//loadingPageProcess('HIDE', null);
	    }   
	});
}
function addItemsToList(whatToProcess, dataToProcess)
{
	var max_cols,div,linkDiv,anchor,row,cell,header_text,select,option,tr,th,thead,text,table,tbody,which_inn,total_runs,plyr;
	
	switch (whatToProcess) {
	case 'LOAD_HISTORIC':

		$('#extra_log_event_row_1').empty();
	
		select = document.createElement('select');
		select.id = 'select_historic_inning';
		select.name = select.id;
		match_data.match.inning.forEach(function(inn,index,arr){
			option = document.createElement('option');
			option.value = inn.inningNumber;
		    option.text = inn.inningNumber;
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
			    option.selected = true;
			}
		    select.appendChild(option);
		});
		header_text = document.createElement('label');
		header_text.innerHTML = 'Inning: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(0).appendChild(header_text).appendChild(select);	
	
		option = document.createElement('input');
		option.type = 'text';
		option.id = 'select_historic_jump_over';
		header_text = document.createElement('label');
		header_text.innerHTML = 'Jump To Over:';
		header_text.htmlFor = option.id;
		document.getElementById('extra_log_event_row_1').insertCell(1).appendChild(header_text).appendChild(option);
	
		option = document.createElement('input');
		option.type = 'button';
		option.name = 'load_historic_btn';
		option.value = 'Historic Point';
		option.id = option.name;
		if(match_data.setup.historicMatchLoaded) {
			option.style.display = 'none';
		} else {
			option.style.display = '';
		}
		option.setAttribute('onclick','processUserSelection(this)');
	
		div = document.createElement('div');
		div.append(option);
	
		option = document.createElement('input');
		option.type = 'button';
		option.name = 'cancel_historic_btn';
		option.id = option.name;
		option.value = 'Cancel';
		option.style.marginTop = '8px'; 
		option.setAttribute('onclick','processUserSelection(this)');
	
		div.append(document.createElement('br'));
		div.append(option);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'restore_match_btn';
		option.id = option.name;
		option.value = 'Restore';
		if(match_data.setup.historicMatchLoaded) {
			option.style.display = '';
		} else {
			option.style.display = 'none';
		}
		option.style.marginTop = '8px'; 
		option.setAttribute('onclick','processUserSelection(this)');
	
		div.append(document.createElement('br'));
		div.append(option);
	
		document.getElementById('extra_log_event_row_1').insertCell(2).appendChild(div);
		break;
		
	case 'POPULATE_PP_TABLE':

		if(document.getElementById('extra_log_event_row_1').querySelector('table')) {
			document.getElementById('extra_log_event_row_1').querySelector('table').remove();
		}

		table = document.createElement('table');
		table.setAttribute('class', 'table table-bordered');
		tr = document.createElement('tr');
		for (var j = 0; j <= 3; j++) {
		    th = document.createElement('th'); //column
		    switch (j) {
			case 0:
			    text = document.createTextNode('Inn'); 
				break;
			case 1:
			    text = document.createTextNode('PP1'); 
				break;
			case 2:
			    text = document.createTextNode('PP2'); 
				break;
			case 3:
			    text = document.createTextNode('PP3'); 
				break;
			}
		    th.appendChild(text);
		    tr.appendChild(th);
		}
		thead = document.createElement('thead');
		thead.appendChild(tr);
		table.appendChild(thead);		
	
		tbody = document.createElement('tbody');
		table.appendChild(tbody);
		
		match_data.match.inning.forEach(function(inn,index,arr){
			row = tbody.insertRow(tbody.rows.length);
			cell = row.insertCell(0);
			cell.innerHTML = inn.inningNumber;
			cell = row.insertCell(1);
			cell.innerHTML = inn.firstPowerplayStartOver + ' to ' + inn.firstPowerplayEndOver;
			cell = row.insertCell(2);
			cell.innerHTML = inn.secondPowerplayStartOver + ' to ' + inn.secondPowerplayEndOver;
			cell = row.insertCell(3);
			cell.innerHTML = inn.thirdPowerplayStartOver + ' to ' + inn.thirdPowerplayEndOver;
		});

		div = document.createElement('div');
		div.append(table);
		document.getElementById('extra_log_event_row_1').cells[6].appendChild(div);
		normalizeAndAppendTable(table, div, 'extra_log_event_row_1');

		table.style.tableLayout = 'auto';
		table.style.width = 'auto';
		
		document.getElementById('select_number_of_pps').value = match_data.setup.numberOfPowerplays;
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.inningNumber == $('#select_pp_inning').val()) {
				if ($('#select_pp_number').val() == 1) {
					document.getElementById('select_pp_start_over_number').value = inn.firstPowerplayStartOver;
					document.getElementById('select_pp_end_over_number').value = inn.firstPowerplayEndOver;
				} else if ($('#select_pp_number').val() == 2) {
					document.getElementById('select_pp_start_over_number').value = inn.secondPowerplayStartOver;
					document.getElementById('select_pp_end_over_number').value = inn.secondPowerplayEndOver;
				} else if ($('#select_pp_number').val() == 3) {
					document.getElementById('select_pp_start_over_number').value = inn.thirdPowerplayStartOver;
					document.getElementById('select_pp_end_over_number').value = inn.thirdPowerplayEndOver;
				} 
			}
		});
		
		break;		
		
/*	case 'POPULATE_PP_TABLE':

		$('#extra_log_event_row_2').empty();
			
		table = document.createElement('table');
		table.setAttribute('class', 'table table-bordered');
		tr = document.createElement('tr');
		for (var j = 0; j <= 3; j++) {
		    th = document.createElement('th'); //column
		    switch (j) {
			case 0:
			    text = document.createTextNode('Inning'); 
				break;
			case 1:
			    text = document.createTextNode('PP1'); 
				break;
			case 2:
			    text = document.createTextNode('PP2'); 
				break;
			case 3:
			    text = document.createTextNode('PP3'); 
				break;
			}
		    th.appendChild(text);
		    tr.appendChild(th);
		}
		thead = document.createElement('thead');
		thead.appendChild(tr);
		table.appendChild(thead);		
	
		tbody = document.createElement('tbody');
		table.appendChild(tbody);
		
		match_data.match.inning.forEach(function(inn,index,arr){
			row = tbody.insertRow(tbody.rows.length);
			cell = row.insertCell(0);
			cell.innerHTML = inn.inningNumber;
			cell = row.insertCell(1);
			cell.innerHTML = inn.firstPowerplayStartOver + ' to ' + inn.firstPowerplayEndOver;
			cell = row.insertCell(2);
			cell.innerHTML = inn.secondPowerplayStartOver + ' to ' + inn.secondPowerplayEndOver;
			cell = row.insertCell(3);
			cell.innerHTML = inn.thirdPowerplayStartOver + ' to ' + inn.thirdPowerplayEndOver;
		});

		div = document.createElement('div');
		div.append(table);
		normalizeAndAppendTable(table, div, 'extra_log_event_row_2');
		//table.querySelectorAll("td, th").forEach(c => c.style.whiteSpace = "normal");
		
		document.getElementById('extra_log_event_row_2').insertCell(0).appendChild(div);
		document.getElementById('extra_log_event_row_2').style.display = '';

		document.getElementById('select_number_of_pps').value = match_data.setup.numberOfPowerplays;
			
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.inningNumber == $('#select_pp_inning').val()) {
				if ($('#select_pp_number').val() == 1) {
					document.getElementById('select_pp_start_over_number').value = inn.firstPowerplayStartOver;
					document.getElementById('select_pp_end_over_number').value = inn.firstPowerplayEndOver;
				} else if ($('#select_pp_number').val() == 2) {
					document.getElementById('select_pp_start_over_number').value = inn.secondPowerplayStartOver;
					document.getElementById('select_pp_end_over_number').value = inn.secondPowerplayEndOver;
				} else if ($('#select_pp_number').val() == 3) {
					document.getElementById('select_pp_start_over_number').value = inn.thirdPowerplayStartOver;
					document.getElementById('select_pp_end_over_number').value = inn.thirdPowerplayEndOver;
				} 
			}
		});
		
		break;*/
		
	case 'LOAD_PP':
		
		$('#extra_log_event_row_1').empty();

		select = document.createElement('select');
		select.id = 'select_number_of_pps';
		select.name = select.id;
		max_cols = 4;
		for(var i=0; i<=max_cols; i++) {
			option = document.createElement('option');
			option.value = i;
		    option.text = i;
		    select.appendChild(option);
		}
		header_text = document.createElement('label');
		header_text.innerHTML = 'Number Of PPs ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(0).appendChild(header_text).appendChild(select);	
		
		select = document.createElement('select');
		select.id = 'select_pp_inning';
		select.name = select.id;
		match_data.match.inning.forEach(function(inn,index,arr){
			option = document.createElement('option');
			option.value = inn.inningNumber;
		    option.text = inn.inningNumber;
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
			    option.selected = true;
			}
		    select.appendChild(option);
		});
		select.setAttribute('onchange','processUserSelection(this);');
		header_text = document.createElement('label');
		header_text.innerHTML = 'Inning ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(1).appendChild(header_text).appendChild(select);	
		
		select = document.createElement('select');
		select.id = 'select_pp_number';
		select.name = select.id;
		max_cols = 3;
		for(var i=1; i<=max_cols; i++) {
			option = document.createElement('option');
			option.value = i;
		    option.text = getFullOrdinalText(i) + ' powerplay';
		    select.appendChild(option);
		}
		select.setAttribute('onchange','processUserSelection(this);');
		header_text = document.createElement('label');
		header_text.innerHTML = 'Choose PP ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(2).appendChild(header_text).appendChild(select);	

		option = document.createElement('input');
		option.type = 'text';
		option.id = 'select_pp_start_over_number';
		option.style.width = "50px"; 
		header_text = document.createElement('label');
		header_text.innerHTML = 'Start Over ';
		header_text.htmlFor = option.id;
		document.getElementById('extra_log_event_row_1').insertCell(3).appendChild(header_text).appendChild(option);

		option = document.createElement('input');
		option.type = 'text';
		option.id = 'select_pp_end_over_number';
		option.style.width = "50px"; 
		header_text = document.createElement('label');
		header_text.innerHTML = 'End Over ';
		header_text.htmlFor = option.id;
		document.getElementById('extra_log_event_row_1').insertCell(4).appendChild(header_text).appendChild(option);
		
	    option = document.createElement('input');
	    option.type = 'button';
	    option.name = 'log_pp_btn';
	    option.value = 'Log PP';
	    option.id = option.name;
		option.setAttribute('onclick','processUserSelection(this)');

	    div = document.createElement('div');
	    div.append(option);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'cancel_pp_btn';
		option.id = option.name;
		option.value = 'Cancel';
		option.style.marginTop = '8px'; 
		option.setAttribute('onclick','processUserSelection(this)');

	    div.append(option);
	    
	    document.getElementById('extra_log_event_row_1').insertCell(5).appendChild(div);

		header_text = document.createElement('label');
		header_text.innerHTML = ''; //Empty label for PP table
	    document.getElementById('extra_log_event_row_1').insertCell(6).appendChild(header_text);

		processUserSelection($('#select_pp_inning'));
		
		break;
		
/*	case 'LOAD_FIFTY-FIFTY':
		
		$('#extra_log_event_row_1').empty();

		select = document.createElement('select');
		select.id = 'select_50_50_batsman_id';
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
				inn.battingCard.forEach(function(bc,bc_index,bc_arr){
					if(bc.batsmanInningStarted.toLowerCase() == 'yes') {
						option = document.createElement('option');
						option.value = bc.player.playerId;
					    option.text = bc.player.ticker_name;
					    if(bc.onStrike.toLowerCase() == 'yes') {
						    option.selected = true;
					    }
					    select.appendChild(option);
					}
				});
			}
		});
		header_text = document.createElement('label');
		header_text.innerHTML = 'Choose Challenge batsman: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(0).appendChild(header_text).appendChild(select);	
		
		select = document.createElement('select');
		select.id = 'select_50_50_bowler_id';
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
			    inn.bowlingCard.forEach(function(bc,bc_index,bc_arr){
					option = document.createElement('option');
					option.value = bc.player.playerId;
				    option.text = bc.player.ticker_name;
		    	    if(bc.status.toLowerCase() == 'currentbowler' || bc.status.toLowerCase() == 'lastbowler') {
					    option.selected = true;
				    }
				    select.appendChild(option);
			    });
			}
		});
		header_text = document.createElement('label');
		header_text.innerHTML = 'Choose Challenge bowler: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(1).appendChild(header_text).appendChild(select);	
		
		select = document.createElement('select');
		select.id = 'select_50_50_over_number';
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
				max_cols = inn.totalOvers;
				which_inn = inn.inningNumber;
				if(inn.totalBalls > 0) {
					max_cols = max_cols + 1;
				}
			}
		});
		for(var i=1; i<=max_cols; i++) {
			option = document.createElement('option');
			option.value = i;
		    option.text = 'Over ' + i;
		    option.selected = true;
		    select.appendChild(option);
		}
		select.setAttribute('onchange','processUserSelection(this);');
		header_text = document.createElement('label');
		header_text.innerHTML = 'Select Over:';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(2).appendChild(header_text).appendChild(select);
		
	    select = document.createElement('input');
	    select.type = 'text';
		select.id = 'type_50_50_runs_per_over';
		select.style = 'width:75%';
		header_text = document.createElement('label');
		header_text.innerHTML = 'Runs Scored: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(3).appendChild(header_text).appendChild(select);	
	    select.setAttribute('onkeyup',"processUserSelection(this)");
		
		select = document.createElement('select');
		select.id = 'select_50_50_challenge_runs';
		for(var i=1; i<=100; i++) {
			option = document.createElement('option');
			option.value = i;
		    option.text = i;
			if(i == 16) {
			    option.selected = true;
			}
		    select.appendChild(option);
		}
		select.setAttribute('onchange','processUserSelection(this);');
		header_text = document.createElement('label');
		header_text.innerHTML = 'Challenge Runs: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(4).appendChild(header_text).appendChild(select);	

	    select = document.createElement('input');
	    select.type = 'text';
		select.id = 'select_50_50_bonus_extra_runs';
		select.style = 'width:75%';
		header_text = document.createElement('label');
		header_text.innerHTML = 'Bonus Runs: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(5).appendChild(header_text).appendChild(select);	

	    option = document.createElement('input');
	    option.type = 'button';
	    option.name = 'log_50_50_over_data_btn';
	    option.value = 'Log 50-50 Over Data';
	    option.id = option.name;
		option.setAttribute('onclick','processUserSelection(this)');

		processUserSelection($('#select_50_50_over_number'));
	    
	    div = document.createElement('div');
	    div.append(option);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'cancel_overwrite_btn';
		option.id = option.name;
		option.value = 'Cancel';
		option.style.marginTop = '8px'; 
		option.setAttribute('onclick','processUserSelection(this)');

	    div.append(document.createElement('br'));
	    div.append(option);
	    
	    max_cols = max_cols + 1;
	    document.getElementById('extra_log_event_row_1').insertCell(6).appendChild(div);

		break;*/
		
	case 'LOAD_OVERWRITE_PARTNERSHIPS':
	
		$('#extra_log_event_row_1').empty();

		select = document.createElement('select');
		select.id = 'select_overwrite_partnerships_inning';
		match_data.match.inning.forEach(function(inn,index,arr){
			option = document.createElement('option');
			option.value = inn.inningNumber;
		    option.text = inn.inningNumber;
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
			    option.selected = true;
			}
		    select.appendChild(option);
		});
		select.setAttribute('onchange','processUserSelection(this);');
		header_text = document.createElement('label');
		header_text.innerHTML = 'Choose OVERWRITE inning: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(0).appendChild(header_text).appendChild(select);	
		
		select = document.createElement('select');
		select.id = 'select_overwrite_partnerships';
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.inningNumber == $('#select_overwrite_partnerships_inning option:selected').val()) {
			    if(inn.partnerships != null) {
					inn.partnerships.forEach(function(part,part_index,bc_arr){
						option = document.createElement('option');
						option.value = part.partnershipNumber;
					    option.text = part.firstPlayer.ticker_name + '/' + part.secondPlayer.ticker_name;
					    option.selected = true;
					    select.appendChild(option);
					});
				}
			}
		});
		select.setAttribute('onchange','processUserSelection(this);');
		header_text = document.createElement('label');
		header_text.innerHTML = 'Choose OVERWRITE partnership: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(1).appendChild(header_text).appendChild(select);	
		
		max_cols = 8;
		if(match_data.setup.specialMatchRules != null && match_data.setup.specialMatchRules == 'ISPL'){
			max_cols = 9;
		}
	
		for(var i=1; i<=max_cols; i++) {
		    option = document.createElement('input');
		    option.type = 'text';
		    header_text = document.createElement('label');
			switch (i) {
			case 1:
				option.id = 'overwrite_partnership_first_batter_runs';
				option.value = '0';
				header_text.innerHTML = '1st Bat Runs: ';
				break;
			case 2:
				option.id = 'overwrite_partnership_second_batter_runs';
				option.value = '0';
				header_text.innerHTML = '2nd Bat Runs: ';
				break;
			case 3:
				option.id = 'overwrite_partnership_first_batter_balls';
				option.value = '0';
				header_text.innerHTML = '1st Bat Balls: ';
				break;
			case 4:
				option.id = 'overwrite_partnership_second_batter_balls';
				option.value = '0';
				header_text.innerHTML = '2nd Bat Balls: ';
				break;
			case 5:
				option.id = 'overwrite_partnership_total_runs';
				option.value = '0';
				header_text.innerHTML = 'Total Runs: ';
				break;
			case 6:
				option.id = 'overwrite_partnership_total_balls';
				option.value = '0';
				header_text.innerHTML = 'Total Balls: ';
				break;
			case 7:
				option.id = 'overwrite_partnership_total_fours';
				option.value = '0';
				header_text.innerHTML = 'Total Fours: ';
				break;
			case 8:
				option.id = 'overwrite_partnership_total_sixes';
				option.value = '0';
				header_text.innerHTML = 'Total Sixes: ';
				break;
			case 9:
				option.id = 'overwrite_partnership_total_nines';
				option.value = '0';
				header_text.innerHTML = 'Total Nines: ';
				break;
			}
			option.style = 'width:75%';
			header_text.htmlFor = option.id;
			document.getElementById('extra_log_event_row_1').insertCell(i+1).appendChild(header_text).appendChild(option);
		}
		
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.inningNumber == $('#select_overwrite_partnerships_inning option:selected').val() && inn.partnerships != null) {
			    inn.partnerships.forEach(function(part,part_index,bc_arr){
					if(part.partnershipNumber == $('#select_overwrite_partnerships').val()) {
						for(var i=1; i<=max_cols; i++) {
							switch (i) {
							case 1:
								document.getElementById('overwrite_partnership_first_batter_runs').value = part.firstBatterRuns;
								break;
							case 2:
								document.getElementById('overwrite_partnership_second_batter_runs').value = part.secondBatterRuns;
								break;
							case 3:
								document.getElementById('overwrite_partnership_first_batter_balls').value = part.firstBatterBalls;
								break;
							case 4:
								document.getElementById('overwrite_partnership_second_batter_balls').value = part.secondBatterBalls;
								break;
							case 5:
								document.getElementById('overwrite_partnership_total_runs').value = part.totalRuns;
								break;
							case 6:
								document.getElementById('overwrite_partnership_total_balls').value = part.totalBalls;
								break;
							case 7:
								document.getElementById('overwrite_partnership_total_fours').value = part.totalFours;
								break;
							case 8:
								document.getElementById('overwrite_partnership_total_sixes').value = part.totalSixes;
								break;
							case 9:
								document.getElementById('overwrite_partnership_total_nines').value = part.totalNines;
								break;
							}
						}
					}
				});
			}
		});
		
	    option = document.createElement('input');
	    option.type = 'button';
	    option.name = 'log_partnerships_overwrite_btn';
	    option.value = 'Log Partnerships Overwrite';
	    option.id = option.name;
	    option.setAttribute('onclick','processUserSelection(this);');
	    
	    div = document.createElement('div');
	    div.append(option);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'cancel_overwrite_btn';
		option.id = option.name;
		option.value = 'Cancel';
		option.style.marginTop = '8px'; 
		option.setAttribute('onclick','processUserSelection(this)');

	    div.append(document.createElement('br'));
	    div.append(option);
	    
	    max_cols = max_cols + 1;
	    document.getElementById('extra_log_event_row_1').insertCell(max_cols+1).appendChild(div);

	    break;
		
	case 'LOAD_OVERWRITE_BOWLER_FIGURES':
	
		$('#extra_log_event_row_1').empty();
		
		select = document.createElement('select');
		select.id = 'select_overwrite_bowler_inning';
		match_data.match.inning.forEach(function(inn,index,arr){
			option = document.createElement('option');
			option.value = inn.inningNumber;
		    option.text = inn.inningNumber;
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
			    option.selected = true;
			}
		    select.appendChild(option);
		});
		select.setAttribute('onchange','processUserSelection(this);');
		header_text = document.createElement('label');
		header_text.innerHTML = 'Choose OVERWRITE inning: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(0).appendChild(header_text).appendChild(select);	
		
		select = document.createElement('select');
		select.id = 'select_overwrite_bowler';
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.isCurrentInning.toLowerCase() == 'yes' && inn.bowlingCard != null) {
			    inn.bowlingCard.forEach(function(bc,bc_index,bc_arr){
					option = document.createElement('option');
					option.value = bc.player.playerId;
				    option.text = bc.player.ticker_name;
		    	    if(bc.status != null && bc.status.toLowerCase() == 'currentbowler') {
					    option.selected = true;
				    }
				    select.appendChild(option);
			    });
			}
		});
		select.setAttribute('onchange','processUserSelection(this);');
		header_text = document.createElement('label');
		header_text.innerHTML = 'Choose OVERWRITE bowler: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(1).appendChild(header_text).appendChild(select);	

		max_cols = 8;
		for(var i=1; i<=max_cols; i++) {
		    option = document.createElement('input');
		    option.type = 'text';
		    header_text = document.createElement('label');
			switch (i) {
			case 1:
				option.id = 'overwrite_bowler_overs';
				header_text.innerHTML = 'Overs: ';
				break;
			case 2:
				option.id = 'overwrite_bowler_balls';
				header_text.innerHTML = 'Balls: ';
				break;
			case 3:
				option.id = 'overwrite_bowler_runs';
				header_text.innerHTML = 'Runs: ';
				break;
			case 4:
				option.id = 'overwrite_bowler_wickets';
				header_text.innerHTML = 'Wickets: ';
				break;
			case 5:
				option.id = 'overwrite_bowler_wides';
				header_text.innerHTML = 'Wides: ';
				break;
			case 6:
				option.id = 'overwrite_bowler_no_balls';
				header_text.innerHTML = 'No Balls: ';
				break;
			case 7:
				option.id = 'overwrite_bowler_dots';
				header_text.innerHTML = 'Dots: ';
				break;
			case 8:
				option.id = 'overwrite_bowler_maidens';
				header_text.innerHTML = 'Maidens: ';
				break;
			}
			option.style = 'width:75%';
			header_text.htmlFor = option.id;
			document.getElementById('extra_log_event_row_1').insertCell(i+1).appendChild(header_text).appendChild(option);
		}

		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.inningNumber == $('#select_overwrite_bowler_inning option:selected').val() && inn.bowlingCard != null) {
				inn.bowlingCard.forEach(function(bc,bc_index,bc_arr){
					if(bc.playerId == $('#select_overwrite_bowler').val()) {
						for(var i=1; i<=max_cols; i++) {
							switch (i) {
							case 1:
								document.getElementById('overwrite_bowler_overs').value = bc.overs;
								break;
							case 2:
								document.getElementById('overwrite_bowler_balls').value = bc.balls;
								break;
							case 3:
								document.getElementById('overwrite_bowler_runs').value = bc.runs;
								break;
							case 4:
								document.getElementById('overwrite_bowler_wickets').value = bc.wickets;
								break;
							case 5:
								document.getElementById('overwrite_bowler_wides').value = bc.wides;
								break;
							case 6:
								document.getElementById('overwrite_bowler_no_balls').value = bc.noBalls;
								break;
							case 7:
								document.getElementById('overwrite_bowler_dots').value = bc.dots;
								break;
							case 8:
								document.getElementById('overwrite_bowler_maidens').value = bc.maidens;
								break;
							}
						}
					}
				});
			}
		});

		select = document.createElement('select');
		select.id = 'select_overwrite_bowler_status';
		select.name = select.id;
		option = document.createElement('option');
		option.value = 'CURRENTBOWLER';
	    option.text = 'Current Bowler';
	    select.appendChild(option);
		option = document.createElement('option');
		option.value = 'LASTBOWLER';
	    option.text = 'Last Bowler';
	    select.appendChild(option);
		option = document.createElement('option');
		option.value = 'OTHERBOWLER';
	    option.text = 'Other Bowler';
	    select.appendChild(option);
		header_text = document.createElement('label');
		header_text.innerHTML = 'Status: ';
		header_text.htmlFor = select.id;
	    max_cols = max_cols + 1;
		document.getElementById('extra_log_event_row_1').insertCell(max_cols+1).appendChild(header_text).appendChild(select);	
		
		select = document.createElement('select');
		select.id = 'select_overwrite_bowlingcard_delete_bowler';
		option = document.createElement('option');
		option.value = 'no';
		option.text = 'No';
		select.appendChild(option);
		option = document.createElement('option');
		option.value = 'yes';
		option.text = 'Yes';
		select.appendChild(option);

		header_text = document.createElement('label');
		header_text.innerHTML = 'Delete: ';
		header_text.htmlFor = select.id;
		max_cols = max_cols + 1;
		document.getElementById('extra_log_event_row_1').insertCell(max_cols+1).appendChild(header_text).appendChild(select);	
	
	    option = document.createElement('input');
	    option.type = 'button';
	    option.name = 'log_bowler_figures_overwrite_btn';
	    option.value = 'Log Bowler';
	    option.id = option.name;
	    option.setAttribute('onclick','processUserSelection(this);');
	    
	    div = document.createElement('div');
	    div.append(option);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'cancel_overwrite_btn';
		option.id = option.name;
		option.value = 'Cancel';
		option.style.marginTop = '8px'; 
		option.setAttribute('onclick','processUserSelection(this)');

	    div.append(document.createElement('br'));
	    div.append(option);
	    
	    max_cols = max_cols + 1;
	    document.getElementById('extra_log_event_row_1').insertCell(max_cols+1).appendChild(div);

	    break;

	case 'LOAD_OVERWRITE_BATSMAN_STATS':
	
		$('#extra_log_event_row_1').empty();
		
		select = document.createElement('select');
		select.id = 'select_overwrite_batsman_stats_inning';
		match_data.match.inning.forEach(function(inn,index,arr){
			option = document.createElement('option');
			option.value = inn.inningNumber;
		    option.text = inn.inningNumber;
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
			    option.selected = true;
			}
		    select.appendChild(option);
		});
		select.setAttribute('onchange','processUserSelection(this);');
		header_text = document.createElement('label');
		header_text.innerHTML = 'Choose OVERWRITE inning: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(0).appendChild(header_text).appendChild(select);	
		
		select = document.createElement('select');
		select.id = 'select_overwrite_batsman';
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.isCurrentInning.toLowerCase() == 'yes' && inn.battingCard != null) {
				inn.battingCard.forEach(function(bc,bc_index,bc_arr){
					if(bc.batsmanInningStarted.toLowerCase() == 'yes') {
						option = document.createElement('option');
						option.value = bc.player.playerId;
					    option.text = bc.player.ticker_name;
					    if(bc.onStrike.toLowerCase() == 'yes') {
						    option.selected = true;
					    }
					    select.appendChild(option);
					}
				});
			}
		});
	    select.setAttribute('onchange','processUserSelection(this);');
		header_text = document.createElement('label');
		header_text.innerHTML = 'Choose OVERWRITE batsman: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(1).appendChild(header_text).appendChild(select);	
		
		max_cols = 6;
		if(match_data.setup.specialMatchRules != null && match_data.setup.specialMatchRules == 'ISPL'){
			max_cols = 7;
		}
		
		for(var i=1; i<=max_cols; i++) {
			
		    option = document.createElement('input');
		    option.type = 'text';
			option.value = '0';

		    header_text = document.createElement('label');
			switch (i) {
			case 1:
				option.id = 'overwrite_batsman_runs';
				header_text.innerHTML = 'Runs: ';
				break;
			case 2:
				option.id = 'overwrite_batsman_balls';
				header_text.innerHTML = 'Balls: ';
				break;
			case 3:
				option.id = 'overwrite_batsman_fours';
				header_text.innerHTML = 'Fours: ';
				break;
			case 4:
				option.id = 'overwrite_batsman_sixes';
				header_text.innerHTML = 'Sixes: ';
				break;
			case 5:
				option.id = 'overwrite_batsman_on_strike';
				header_text.innerHTML = 'On Strike (Yes/No): ';
				break;
			case 6:
				option.id = 'overwrite_batsman_minutes';
				header_text.innerHTML = 'Minutes: ';
				break;
			case 7:
				option.id = 'overwrite_batsman_nines';
				header_text.innerHTML = 'Nines: ';
				break;
			}
			option.style = 'width:75%';
			header_text.htmlFor = option.id;
			document.getElementById('extra_log_event_row_1').insertCell(i+1).appendChild(header_text).appendChild(option);
		}
		
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.inningNumber == $('#select_overwrite_batsman_stats_inning option:selected').val()) {
				inn.battingCard.forEach(function(bc,bc_index,bc_arr){
					if(bc.playerId == $('#select_overwrite_batsman').val()) {
						for(var i=1; i<=max_cols; i++) {
							switch (i) {
							case 1:
								document.getElementById('overwrite_batsman_runs').value = bc.runs;
								break;
							case 2:
								document.getElementById('overwrite_batsman_balls').value = bc.balls;
								break;
							case 3:
								document.getElementById('overwrite_batsman_fours').value = bc.fours;
								break;
							case 4:
								document.getElementById('overwrite_batsman_sixes').value = bc.sixes;
								break;
							case 5:
								document.getElementById('overwrite_batsman_on_strike').value = bc.onStrike;
								break;
							case 6:
								document.getElementById('overwrite_batsman_minutes').value = Math.floor(bc.duration / 60);
								break;
							case 7:
								document.getElementById('overwrite_batsman_nines').value = bc.nines;
								break;
							}
						}
					}
				});
			}
		});
		
	    option = document.createElement('input');
	    option.type = 'button';
	    option.name = 'log_batsman_stats_overwrite_btn';
	    option.value = 'Log Batsman Stats Overwrite';
	    option.id = option.name;
	    option.setAttribute('onclick','processUserSelection(this);');
	    
	    div = document.createElement('div');
	    div.append(option);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'cancel_overwrite_btn';
		option.id = option.name;
		option.value = 'Cancel';
		option.style.marginTop = '8px'; 
		option.setAttribute('onclick','processUserSelection(this)');

	    div.append(document.createElement('br'));
	    div.append(option);
	    
	    document.getElementById('extra_log_event_row_1').insertCell(
			document.getElementById('extra_log_event_row_1').cells.length).appendChild(div);
		
		break;

/*	case 'LOAD_OVERWRITE_SUBSTITUTION':
	
		$('#extra_log_event_row_1').empty();
		
		select = document.createElement('select');
		select.id = 'select_current_substitution_index';
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
				inn.battingCard.forEach(function(bc,bc_index,bc_arr){
					option = document.createElement('option');
					option.value = bc.player.playerId;
				    option.text = bc.player.ticker_name;
				    if(bc.onStrike.toLowerCase() == 'yes') {
					    option.selected = true;
				    }
				    select.appendChild(option);
				});
			}
		});
		header_text = document.createElement('label');
		header_text.innerHTML = 'Choose CURRENT batsman: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(0).appendChild(header_text).appendChild(select);	
		
		select = document.createElement('select');
		select.id = 'select_overwrite_substitution_index';
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
				if(inn.battingTeamId == match_data.setup.homeTeamId) {
					match_data.setup.homeSubstitutes.forEach(function(hs,bc_index,bc_arr){
						option = document.createElement('option');
						option.value = hs.playerId;
					    option.text = hs.ticker_name;
					    select.appendChild(option);
					});
				}else if(inn.battingTeamId == match_data.setup.awayTeamId) {
					match_data.setup.awaySubstitutes.forEach(function(as,bc_index,bc_arr){
						option = document.createElement('option');
						option.value = as.playerId;
					    option.text = as.ticker_name;
					    select.appendChild(option);
					});
				}
			}
		});
		header_text = document.createElement('label');
		header_text.innerHTML = 'Choose OVERWRITE batsman: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(1).appendChild(header_text).appendChild(select);	

		select = document.createElement('select');
		select.id = 'select_overwrite_substitution_position';
		for(var i = 1; i <= 11; i++) {
			option = document.createElement('option');
			option.value = i;
		    option.text = i;
		    select.appendChild(option);
		}
		header_text = document.createElement('label');
		header_text.innerHTML = 'Choose OVERWRITE position: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(2).appendChild(header_text).appendChild(select);	

		select = document.createElement('select');
		select.id = 'select_overwrite_substitution_reason';
		for(var i = 1; i <= 3; i++) {
			option = document.createElement('option');
			switch(i) {
			case 1:
				option.value = '';
			    option.text = '';
				break;
			case 2:
				option.value = 'impact';
			    option.text = 'Impact Player';
				break;
			case 3:
				option.value = 'concussion';
			    option.text = 'Concussion';
				break;
			}
		    select.appendChild(option);
		}
		header_text = document.createElement('label');
		header_text.innerHTML = 'Choose OVERWRITE reason: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(3).appendChild(header_text).appendChild(select);	

	    option = document.createElement('input');
	    option.type = 'button';
	    option.name = 'log_substitution_overwrite_btn';
	    option.value = 'Log Batting Card Overwrite';
	    option.id = option.name;
	    option.setAttribute('onclick','processUserSelection(this);');
	    
	    div = document.createElement('div');
	    div.append(option);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'cancel_overwrite_btn';
		option.id = option.name;
		option.value = 'Cancel';
		option.style.marginTop = '8px'; 
		option.setAttribute('onclick','processUserSelection(this)');

	    div.append(document.createElement('br'));
	    div.append(option);
	    
	    document.getElementById('extra_log_event_row_1').insertCell(4).appendChild(div);
		
		break;*/

	case 'LOAD_OVERWRITE_BATTINGCARD':
	
		$('#extra_log_event_row_1').empty();
		
		select = document.createElement('select');
		select.id = 'select_overwrite_batting_card_inning';
		match_data.match.inning.forEach(function(inn,index,arr){
			option = document.createElement('option');
			option.value = inn.inningNumber;
		    option.text = inn.inningNumber;
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
			    option.selected = true;
			}
		    select.appendChild(option);
		});
		select.setAttribute('onchange','processUserSelection(this);');
		header_text = document.createElement('label');
		header_text.innerHTML = 'Choose OVERWRITE inning: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(0).appendChild(header_text).appendChild(select);		
		
		select = document.createElement('select');
		select.id = 'select_current_battingcard_index';
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.inningNumber == $('#select_overwrite_batting_card_inning option:selected').val()) {
				inn.battingCard.forEach(function(bc,bc_index,bc_arr){
					option = document.createElement('option');
					option.value = bc.player.playerId;
				    option.text = bc.player.ticker_name;
				    if(bc.onStrike.toLowerCase() == 'yes') {
					    option.selected = true;
				    }
				    select.appendChild(option);
				});
			}
		});
		header_text = document.createElement('label');
		header_text.innerHTML = 'Choose CURRENT batsman: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(1).appendChild(header_text).appendChild(select);	

		select = document.createElement('select');
		select.id = 'select_overwrite_battingcard_index';
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.inningNumber == $('#select_overwrite_batting_card_inning option:selected').val()) {
				inn.battingCard.forEach(function(bc,bc_index,bc_arr){
					option = document.createElement('option');
					option.value = bc.player.playerId;
				    option.text = bc.player.ticker_name;
				    if(bc.onStrike.toLowerCase() == 'yes') {
					    option.selected = true;
				    }
				    select.appendChild(option);
				});
			}
		});
		header_text = document.createElement('label');
		header_text.innerHTML = 'Choose SWAP batsman: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(2).appendChild(header_text).appendChild(select);	

		select = document.createElement('select');
		select.id = 'select_overwrite_battingcard_delete_batter';
		option = document.createElement('option');
		option.value = 'no';
		option.text = 'No';
		select.appendChild(option);
		option = document.createElement('option');
		option.value = 'yes';
		option.text = 'Yes';
		select.appendChild(option);

		header_text = document.createElement('label');
		header_text.innerHTML = 'Delete batter: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(3).appendChild(header_text).appendChild(select);	

		option = document.createElement('input');
	    option.type = 'button';
	    option.name = 'log_battingcard_overwrite_btn';
	    option.value = 'Log Batting Card Overwrite';
	    option.id = option.name;
	    option.setAttribute('onclick','processUserSelection(this);');
	    
	    div = document.createElement('div');
	    div.append(option);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'cancel_overwrite_btn';
		option.id = option.name;
		option.value = 'Cancel';
		option.style.marginTop = '8px'; 
		option.setAttribute('onclick','processUserSelection(this)');

	    div.append(document.createElement('br'));
	    div.append(option);
	    
	    document.getElementById('extra_log_event_row_1').insertCell(4).appendChild(div);
		
		break;

	case 'LOAD_OVERWRITE_BATSMAN_HOWOUT':
	
		$('#extra_log_event_row_1').empty();

		select = document.createElement('select');
		select.id = 'select_overwrite_batsman_out_inning';
		match_data.match.inning.forEach(function(inn,index,arr){
			option = document.createElement('option');
			option.value = inn.inningNumber;
		    option.text = inn.inningNumber;
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
			    option.selected = true;
			}
		    select.appendChild(option);
		});
		select.setAttribute('onchange','processUserSelection(this);');
		header_text = document.createElement('label');
		header_text.innerHTML = 'Inning: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(0).appendChild(header_text).appendChild(select);

		select = document.createElement('select');
		select.id = 'select_overwrite_batsman_out';
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.inningNumber == $('#select_overwrite_batsman_out_inning option:selected').val()) {
				inn.battingCard.forEach(function(bc,bc_index,bc_arr){
					if(bc.batsmanInningStarted.toLowerCase() == 'yes') {
						option = document.createElement('option');
						option.value = bc.player.playerId;
					    option.text = bc.player.ticker_name;
					    if(bc.status.toLowerCase() == 'out') {
						    option.selected = true;
					    }
					    select.appendChild(option);
					}
				});
			}
		});
	    select.setAttribute('onchange','processUserSelection(this);');
		header_text = document.createElement('label');
		header_text.innerHTML = 'Batsman: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(1).appendChild(header_text).appendChild(select);	
	
		select = document.createElement('select');
		select.id = 'select_overwrite_batsman_howout';
		for(var i=0;i<=16;i++) {
			option = document.createElement('option');
			switch (i) {
			case 0:
			    option.value = '';
				break;
			case 1:
			    option.value = 'caught';
				break;
			case 2:
			    option.value = 'caught_and_bowled';
				break;
			case 3:
			    option.value = 'bowled';
				break;
			case 4:
			    option.value = 'lbw';
				break;
			case 5:
			    option.value = 'stumped';
				break;
			case 6:
			    option.value = 'run_out';
				break;
			case 7:
			    option.value = 'hit_wicket';
				break;
			case 8:
			    option.value = 'handled_the_ball';
				break;
			case 9:
			    option.value = 'hit_ball_twice';
				break;
			case 10:
			    option.value = 'obstructing_fielder';
				break;
			case 11:
			    option.value = 'timed_out';
				break;
			case 12:
			    option.value = 'retired_hurt';
				break;
			case 13:
			    option.value = 'mankad';
				break;
			case 14:
			    option.value = 'absent_hurt';
				break;
			case 15:
			    option.value = 'concussed';
				break;
			case 16:
			    option.value = 'retired_out';
				break;
			}
		    option.text = option.value.replace(/(^\w{1})|(\s{1}\w{1})/g, match => match.toUpperCase()).replace('_',' ');
		    select.appendChild(option);
		}
		
		header_text = document.createElement('label');
		header_text.innerHTML = 'How out: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(2).appendChild(header_text).appendChild(select);
		//$('#' + select.id).select2({dropdownAutoWidth : true});
		
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.inningNumber == $('#select_overwrite_batsman_out_inning option:selected').val()) {
				select = document.createElement('select');
				select.id = 'select_overwrite_batsman_concussionPlayerId';
				option = document.createElement('option');
				option.value = '';
			    option.text = '';
			    select.appendChild(option);
				if(inn.battingTeamId == match_data.setup.homeTeamId && match_data.setup.homeOtherSquad != null) {
					match_data.setup.homeOtherSquad.forEach(function(hos,index,arr){
						option = document.createElement('option');
						option.value = hos.playerId;
					    option.text = hos.ticker_name;
					    select.appendChild(option);
					});
				} else if(inn.battingTeamId == match_data.setup.awayTeamId && match_data.setup.awayOtherSquad != null) {
					match_data.setup.awayOtherSquad.forEach(function(hos,index,arr){
						option = document.createElement('option');
						option.value = hos.playerId;
					    option.text = hos.ticker_name;
					    select.appendChild(option);
					});
				}
				header_text = document.createElement('label');
				header_text.innerHTML = 'REPLACEMENT Batsman: ';
				header_text.htmlFor = select.id;
				document.getElementById('extra_log_event_row_1').insertCell(3).appendChild(header_text).appendChild(select);
				//$('#' + select.id).select2({dropdownAutoWidth : true});

				select = document.createElement('select');
				select.id = 'select_overwrite_batsman_howOutFielderId';
				if(inn.fielders != null) {
					inn.fielders.forEach(function(field,index,arr){
						option = document.createElement('option');
						option.value = field.playerId;
					    option.text = field.ticker_name;
					    if(field.captainWicketKeeper.toLowerCase().includes('wicket_keeper')) {
						    option.selected = true;
					    }
					    select.appendChild(option);
					});
				}
				if(inn.bowlingTeamId == match_data.setup.homeTeamId && match_data.setup.homeOtherSquad != null) {
					match_data.setup.homeOtherSquad.forEach(function(field,index,arr){
						option = document.createElement('option');
						option.value = field.playerId;
					    option.text = field.ticker_name + ' (SUB)';
					    select.appendChild(option);
					});
				} else if(inn.bowlingTeamId == match_data.setup.awayTeamId && match_data.setup.awayOtherSquad != null) {
					match_data.setup.awayOtherSquad.forEach(function(field,index,arr){
						option = document.createElement('option');
						option.value = field.playerId;
					    option.text = field.ticker_name + ' (SUB)';
					    select.appendChild(option);
					});
				}
				//Don't know fielder (substitute)
				option = document.createElement('option');
				option.value = -1;
			    option.text = "Substitute (Don't Know)";
			    select.appendChild(option);
				
				header_text = document.createElement('label');
				header_text.innerHTML = 'Fielder ';
				header_text.htmlFor = select.id;
				document.getElementById('extra_log_event_row_1').insertCell(4).appendChild(header_text).appendChild(select);
				
				select = document.createElement('select');
				select.id = 'select_overwrite_batsman_howOutBowlerId';
				if(inn.bowlingCard != null) {
					inn.bowlingCard.forEach(function(bc,index,arr){
						option = document.createElement('option');
						option.value = bc.playerId;
					    option.text = bc.player.ticker_name;
					    select.appendChild(option);
					});
				}
				header_text = document.createElement('label');
				header_text.innerHTML = 'Bowler: ';
				header_text.htmlFor = select.id;
				document.getElementById('extra_log_event_row_1').insertCell(5).appendChild(header_text).appendChild(select);
				
			}
		});
		
		select = document.createElement('select');
		select.id = 'select_overwrite_batsman_out_substitute';
		option = document.createElement('option');
		option.value = 'NO';
	    option.text = 'NO';
	    select.appendChild(option);
		option = document.createElement('option');
		option.value = 'YES';
	    option.text = 'YES';
	    select.appendChild(option);
		header_text = document.createElement('label');
		header_text.innerHTML = 'Choose SUBSTITUTE: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(6).appendChild(header_text).appendChild(select);
		
		processUserSelection($('#select_overwrite_batsman_out'));	
		
	    option = document.createElement('input');
	    option.type = 'button';
	    option.name = 'log_batsman_howout_overwrite_btn';
	    option.value = 'Log Howout';
	    option.id = option.name;
	    option.setAttribute('onclick','processUserSelection(this);');
	    
	    div = document.createElement('div');
	    div.append(option);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'cancel_overwrite_btn';
		option.id = option.name;
		option.value = 'Cancel';
		option.style.marginTop = '8px'; 
		option.setAttribute('onclick','processUserSelection(this)');

	    div.append(document.createElement('br'));
	    div.append(option);
	    
	    document.getElementById('extra_log_event_row_1').insertCell(7).appendChild(div);
		
		break;

	case 'LOAD_OVERWRITE_TEAMS_TOTAL': case 'LOAD_OVERWRITE_TEAMS_EXTRAS': 

		$('#extra_log_event_row_1').empty();

		select = document.createElement('select');
		select.id = 'select_overwrite_team_stats_inning';
		match_data.match.inning.forEach(function(inn,index,arr){
			option = document.createElement('option');
			option.value = inn.inningNumber;
		    option.text = inn.inningNumber;
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
			    option.selected = true;
			}
		    select.appendChild(option);
		});
		select.setAttribute('onchange','processUserSelection(this);');
		header_text = document.createElement('label');
		header_text.innerHTML = 'Choose OVERWRITE inning: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(0).appendChild(header_text).appendChild(select);
		
		max_cols = 5;
		switch (whatToProcess) {
		case 'LOAD_OVERWRITE_TEAMS_TOTAL':
			if(match_data.setup.specialMatchRules != null && match_data.setup.specialMatchRules == 'ISPL'){
				max_cols = 7;
			}
			break;
		}
		
		for(var i=0; i<=max_cols; i++) {
			
		    option = document.createElement('input');
		    option.type = 'text';

		    header_text = document.createElement('label');
			
			match_data.match.inning.forEach(function(inn,index,arr){
				if(inn.inningNumber == $('#select_overwrite_team_stats_inning option:selected').val()) {
					switch (whatToProcess) {
					case 'LOAD_OVERWRITE_TEAMS_TOTAL':
						switch (i) {
						case 0:
							option.id = 'overwrite_total_runs';
							option.value = inn.totalRuns;
							header_text.innerHTML = 'Total Runs: ';
							break;
						case 1:
							option.id = 'overwrite_total_wickets';
							option.value = inn.totalWickets;
							header_text.innerHTML = 'Total Wickets: ';
							break;
						case 2:
							option.id = 'overwrite_total_overs';
							option.value = inn.totalOvers;
							header_text.innerHTML = 'Total Overs: ';
							break;
						case 3:
							option.id = 'overwrite_total_balls';
							option.value = inn.totalBalls;
							header_text.innerHTML = 'Total Balls: ';
							break;
						case 4:
							option.id = 'overwrite_total_fours';
							option.value = inn.totalFours;
							header_text.innerHTML = 'Total Fours: ';
							break;
						case 5:
							option.id = 'overwrite_total_sixes';
							option.value = inn.totalSixes;
							header_text.innerHTML = 'Total Sixes: ';
							break;
						case 6:
							option.id = 'overwrite_total_nines';
							option.value = inn.totalNines;
							header_text.innerHTML = 'Total Nines: ';
							break;
						case 7:
							option.id = 'overwrite_team_special_runs';
							option.value = inn.specialRuns;
							header_text.innerHTML = 'Special Runs: ';
							break;
						}
						break;
					case 'LOAD_OVERWRITE_TEAMS_EXTRAS':
						switch (i) {
						case 0:
							option.id = 'overwrite_total_wides';
							option.value = inn.totalWides;
							header_text.innerHTML = 'Total Wides: ';
							break;
						case 1:
							option.id = 'overwrite_total_no_balls';
							option.value = inn.totalNoBalls;
							header_text.innerHTML = 'Total No Balls: ';
							break;
						case 2:
							option.id = 'overwrite_total_byes';
							option.value = inn.totalByes;
							header_text.innerHTML = 'Total Byes: ';
							break;
						case 3:
							option.id = 'overwrite_total_leg_byes';
							option.value = inn.totalLegByes;
							header_text.innerHTML = 'Total Leg Byes: ';
							break;
						case 4:
							option.id = 'overwrite_total_penalties';
							option.value = inn.totalPenalties;
							header_text.innerHTML = 'Total Penalties: ';
							break;
						case 5:
							option.id = 'overwrite_total_extras';
							option.value = inn.totalExtras;
							header_text.innerHTML = 'Total Extras: ';
							break;
						}
						break;
					}
				}
			});
			option.style = 'width:75%';
			header_text.htmlFor = option.id;
			document.getElementById('extra_log_event_row_1').insertCell(i+1).appendChild(header_text).appendChild(option);
		}

	    option = document.createElement('input');
	    option.type = 'button';
		switch (whatToProcess) {
		case 'LOAD_OVERWRITE_TEAMS_TOTAL':
		    option.name = 'log_teams_total_overwrite_btn';
		    option.value = 'Log Team Total Overwrite';
			break;
		case 'LOAD_OVERWRITE_TEAMS_EXTRAS':
		    option.name = 'log_teams_extras_overwrite_btn';
		    option.value = 'Log Team Extras Overwrite';
			break;
		}
	    option.id = option.name;
	    option.setAttribute('onclick','processUserSelection(this);');
	    
	    div = document.createElement('div');
	    div.append(option);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'cancel_overwrite_btn';
		option.id = option.name;
		option.value = 'Cancel';
		option.style.marginTop = '8px'; 
		option.setAttribute('onclick','processUserSelection(this)');

	    div.append(document.createElement('br'));
	    div.append(option);
	    
	    max_cols = max_cols + 1;
	    document.getElementById('extra_log_event_row_1').insertCell(max_cols+1).appendChild(div);
		break;

	case 'LOAD_RESULT':

		$('#extra_log_event_row_1').empty();
		
		select = document.createElement('select');
		select.id = 'select_winning_team';
		for(var iTm = 1; iTm <= 2; iTm++) {
			option = document.createElement('option');
			switch(iTm) {
			case 1:
				option.value = match_data.setup.homeTeamId;
			    option.text = match_data.setup.homeTeam.teamName1;
				break;
			case 2:
				option.value = match_data.setup.awayTeamId;
			    option.text = match_data.setup.awayTeam.teamName1;
				break;
			}
		    select.appendChild(option);
		}
	    header_text = document.createElement('label');
		header_text.innerHTML = 'Winning Team: ';
		document.getElementById('extra_log_event_row_1').insertCell(0).appendChild(header_text).appendChild(select);

	    option = document.createElement('input');
	    option.type = 'text';
		option.id = 'select_winning_margin';
		option.width = '40px';
	    header_text = document.createElement('label');
		header_text.innerHTML = 'Margin: ';
		document.getElementById('extra_log_event_row_1').insertCell(1).appendChild(header_text).appendChild(option);

		select = document.createElement('select');
		select.id = 'select_runs_wickets';
		for(var iRW = 1; iRW <= 6; iRW++) {
			option = document.createElement('option');
			switch(iRW) {
			case 1:
				option.value = 'run';
			    option.text = 'Runs';
				break;
			case 2:
				option.value = 'wicket';
			    option.text = 'Wickets';
				break;
			case 3:
				option.value = 'super_over';
			    option.text = 'Super Over';
				break;
			case 4:
				option.value = 'drawn';
			    option.text = 'Match drawn/tied';
				break;
			case 5:
				option.value = 'abandoned';
			    option.text = 'Match abandoned';
				break;
			case 6:
				option.value = 'no_result';
			    option.text = 'No result';
				break;
			}
		    select.appendChild(option);
		}
	    header_text = document.createElement('label');
		header_text.innerHTML = 'Type: ';
		document.getElementById('extra_log_event_row_1').insertCell(2).appendChild(header_text).appendChild(select);
		
		if(match_data.setup.matchType.toUpperCase() == 'TEST' || match_data.setup.matchType.toUpperCase() == 'FC') {
			
			select = document.createElement('select');
			select.id = 'select_inning_margin';
			for(var iInn = 1; iInn <= 3; iInn++) {
				option = document.createElement('option');
				switch(iInn) {
				case 1:
					option.value = '';
				    option.text = '';
					break;
				case 2:
					option.value = 'inning';
				    option.text = 'Win by inning';
					break;
				case 3:
					option.value = 'inning_lead';
				    option.text = 'Win by 1st inning lead';
					break;
				}
			    select.appendChild(option);
			}
		    header_text = document.createElement('label');
			header_text.innerHTML = 'Inning Margin: ';
			document.getElementById('extra_log_event_row_1').insertCell(3).appendChild(header_text).appendChild(select);
		
		} else {
			
			select = document.createElement('select');
			select.id = 'select_dls_vjd';
			for(var iDls = 1; iDls <= 3; iDls++) {
				option = document.createElement('option');
				switch(iDls) {
				case 1:
					option.value = '';
				    option.text = '';
					break;
				case 2:
					option.value = 'dls';
				    option.text = 'dls';
					break;
				case 3:
					option.value = 'vjd';
				    option.text = 'vjd';
					break;
				}
			    select.appendChild(option);
			}
		    header_text = document.createElement('label');
			header_text.innerHTML = 'DLS Or VJD: ';
			document.getElementById('extra_log_event_row_1').insertCell(3).appendChild(header_text).appendChild(select);
		}	

	    option = document.createElement('input');
	    option.type = 'button';
	    option.name = 'log_result_btn';
	    option.value = 'Log Result';
	    option.id = option.name;
	    option.setAttribute('onclick','processUserSelection(this);');
	    
	    div = document.createElement('div');
	    div.append(option);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'cancel_result_btn';
		option.id = option.name;
		option.value = 'Cancel';
		option.style.marginTop = '8px'; 
		option.setAttribute('onclick','processUserSelection(this)');

	    div.append(document.createElement('br'));
	    div.append(option);
	    
	    document.getElementById('extra_log_event_row_1').insertCell(4).appendChild(div);
		
		break;
		
	case 'LOAD_TOSS':
		
		$('#tossResult').empty();
		
		for(var i=0; i<=3; i++) {
			
			option = document.createElement('option');
			switch (i) {
			case 0:
				option.value = 'home_team_bat';
				option.text = $('#homeTeamId option:selected').text() + ' Won Toss And Bat First';
				break;
			case 1:
				option.value = 'home_team_field';
				option.text = $('#homeTeamId option:selected').text() + ' Won Toss And Field First';
				break;
			case 2:
				option.value = 'away_team_bat';
				option.text = $('#awayTeamId option:selected').text() + ' Won Toss And Bat First';
				break;
			case 3:
				option.value = 'away_team_field';
				option.text = $('#awayTeamId option:selected').text() + ' Won Toss And Field First';
				break;
			}
			document.getElementById('tossResult').appendChild(option);
		}
		if (dataToProcess){
			document.getElementById('tossResult').value = dataToProcess.setup.tossResult;
		} else {
			document.getElementById('tossResult').selectedIndex = 0;
		}
		break;
		
	case 'LOAD_TEAMS':

		$('#team_selection_div').empty();
		document.getElementById('team_selection_div').style.display = 'none';
		
		if (dataToProcess)
		{
			if(dataToProcess.setup.homeSquad.length <=0 || dataToProcess.setup.awaySquad.length <=0) {
				if(dataToProcess.setup.homeSquad.length <=0) {
					alert(dataToProcess.setup.homeTeam.teamName1 + ' has no players in the database');
				} else if(dataToProcess.setup.awaySquad.length <=0) {
					alert(dataToProcess.setup.awayTeam.teamName1 + ' has no players in the database');
				}
				return false;
			}
			table = document.createElement('table');
			table.setAttribute('class', 'table table-bordered');
			table.setAttribute('id', 'setup_teams');
			tr = document.createElement('tr');
			for (var j = 0; j <= 5; j++) {
			    th = document.createElement('th'); //column
			    switch (j) {
				case 0: case 3:
				    text = document.createTextNode('Pos'); 
					break;
				case 1:
				    text = document.createTextNode(dataToProcess.setup.homeTeam.teamName4); 
					break;
				case 2:
				    text = document.createTextNode(dataToProcess.setup.homeTeam.teamName4 + ' captain/keeper'); 
					break;
				case 4:
				    text = document.createTextNode(dataToProcess.setup.awayTeam.teamName4); 
					break;
				case 5:
				    text = document.createTextNode(dataToProcess.setup.awayTeam.teamName4 + ' captain/keeper'); 
					break;
				}
			    th.appendChild(text);
			    tr.appendChild(th);
			}
			
			thead = document.createElement('thead');
			thead.appendChild(tr);
			table.appendChild(thead);

			tbody = document.createElement('tbody');

			max_cols = parseInt(10 + parseInt($('#homeSubstitutesNumber option:selected').val()));
			if(parseInt($('#awaySubstitutesNumber option:selected').val()) > parseInt($('#homeSubstitutesNumber option:selected').val())) {
				max_cols = parseInt(10 + parseInt($('#awaySubstitutesNumber option:selected').val()));
			}
			current_batter = null;
			for(var i=0; i <= max_cols; i++) {
				row = tbody.insertRow(tbody.rows.length);
				for(var j=0; j<=5; j++) {
					switch(j) {
					case 0: case 3:
						select = document.createElement('label');
						select.innerHTML = (i + 1);
						break;
					case 1: case 4:
						select = document.createElement('select');
						select.style = 'width:75%';
						if(j==1) {
							select.name = 'selectHomePlayers';
							select.id = 'homePlayer_' + (i + 1);
							dataToProcess.setup.homeSquad.forEach(function(hp,index,arr){
								option = document.createElement('option');
								option.value = hp.playerId;
							    option.text = hp.full_name;
							    select.appendChild(option);
							});
							if(dataToProcess.setup.homeSubstitutes != null) {
								dataToProcess.setup.homeSubstitutes.forEach(function(hp,index,arr){
									option = document.createElement('option');
									option.value = hp.playerId;
								    option.text = hp.full_name;
								    select.appendChild(option);
								});
							}
							if(dataToProcess.setup.homeOtherSquad != null) {
								dataToProcess.setup.homeOtherSquad.forEach(function(hp,index,arr){
									option = document.createElement('option');
									option.value = hp.playerId;
								    option.text = hp.full_name;
								    select.appendChild(option);
								});
							}
						} else if(j==4) {
							select.name = 'selectAwayPlayers';
							select.id = 'awayPlayer_' + (i + 1);
							dataToProcess.setup.awaySquad.forEach(function(ap,index,arr){
								option = document.createElement('option');
								option.value = ap.playerId;
							    option.text = ap.full_name;
							    select.appendChild(option);
							});
							if(dataToProcess.setup.awaySubstitutes != null) {
								dataToProcess.setup.awaySubstitutes.forEach(function(ap,index,arr){
									option = document.createElement('option');
									option.value = ap.playerId;
								    option.text = ap.full_name;
								    select.appendChild(option);
								});
							}
							if(dataToProcess.setup.awayOtherSquad != null) {
								dataToProcess.setup.awayOtherSquad.forEach(function(ap,index,arr){
									option = document.createElement('option');
									option.value = ap.playerId;
								    option.text = ap.full_name;
								    select.appendChild(option);
								});
							}
						}
					    select.selectedIndex = i;
						if(j==1) {
							if(dataToProcess.setup.setupHomeTeam != null) {
								dataToProcess.setup.setupHomeTeam.split(",").forEach(function (ht) {
									if(ht.split("|")[0] == (i + 1)) {
										select.value = ht.split("|")[1];
									}
								});
							}
						} else if(j==4) {
							if(dataToProcess.setup.setupAwayTeam != null) {
								dataToProcess.setup.setupAwayTeam.split(",").forEach(function (at) {
									if(at.split("|")[0] == (i + 1)) {
										select.value = at.split("|")[1];
									}
								});
							}
						}
						break;
					case 2: case 5:
						select = document.createElement('select');
						select.style = 'width:75%';
						if(j==2) {
							select.name = 'selectHomeCaptainWicketKeeper';
							select.id = 'homeCaptainWicketKeeper_' + (i + 1);
						} else {
							select.name = 'selectAwayCaptainWicketKeeper';
							select.id = 'awayCaptainWicketKeeper_' + (i + 1);
						}
						for(var k=0; k<=3; k++) {
							option = document.createElement('option');
							switch (k) {
							case 0:
								option.value = '';
							    option.text = '';
								break;
							case 1:
								option.value = 'captain';
							    option.text = 'Captain';
								break;
							case 2:
								option.value = 'wicket_keeper';
							    option.text = 'Wicket Keeper';
								break;
							case 3:
								option.value = 'captain_wicket_keeper';
							    option.text = 'Captain And Wicket Keeper';
								break;
							}
						    select.appendChild(option);
						}
						if(i <= 10) {
							if(j==2) {
								if(dataToProcess.setup.setupHomeTeam != null) {
									dataToProcess.setup.setupHomeTeam.split(",").forEach(function (ht) {
										if(ht.split("|")[0] == (i + 1)) {
											dataToProcess.setup.homeSquad.forEach(function (hs) {
												if(ht.split("|")[1] == hs.playerId) {
													select.value = hs.captainWicketKeeper;
												}
											});
										}
									});
								}
							} else if(j==5) {
								if(dataToProcess.setup.setupAwayTeam != null) {
									dataToProcess.setup.setupAwayTeam.split(",").forEach(function (at) {
										if(at.split("|")[0] == (i + 1)) {
											dataToProcess.setup.awaySquad.forEach(function (as) {
												if(at.split("|")[1] == as.playerId) {
													select.value = as.captainWicketKeeper;
												}
											});
										}
									});
								}
							}
						}
						break;
					}
					switch(j) {
					case 0: case 3:  
						cell = row.insertCell(j);
						if(j==0) {
							cell.setAttribute("name", 'selectHomePlayersPosition');
							cell.setAttribute("id", 'homePositionPlayer_' + (i + 1));
						} else {
							cell.setAttribute("name", 'selectAwayPlayersPosition');
							cell.setAttribute("id", 'awayPositionPlayer_' + (i + 1));
						}
						cell.appendChild(select);
						cell.setAttribute('onclick','processUserSelection(this)');
						break;
					case 1: case 2:  
						if(i <= parseInt(10 + parseInt($('#homeSubstitutesNumber option:selected').val()))) {
							row.insertCell(j).appendChild(select);
							$(select).select2();
						} else {
							row.insertCell(j).appendChild(document.createElement('label'));
						}
						removeSelectDuplicates('name', select.name)
						break;
					case 4: case 5:  
						if(i <= parseInt(10 + parseInt($('#awaySubstitutesNumber option:selected').val()))) {
							row.insertCell(j).appendChild(select);
							$(select).select2();
						} else {
							row.insertCell(j).appendChild(document.createElement('label'));
						}
						removeSelectDuplicates('name', select.name)
						break;
					}
				}
			}
			table.appendChild(tbody);
			document.getElementById('team_selection_div').appendChild(table);

			document.getElementById('team_selection_div').style.display = '';
			processUserSelection(document.getElementById('concussionSelected'));
		} 
		break;
		
	case 'LOAD-INNINGS':

		if (dataToProcess) {

			var current_inning = -1;

			$('#select_match_innings').empty();
			dataToProcess.match.inning.forEach(function(inns_item,index,arr){
				option = document.createElement('option');
				option.setAttribute('value', inns_item.inningNumber);
				option.innerHTML = 'Inning ' + inns_item.inningNumber;
				document.getElementById('select_match_innings').appendChild(option);
				if(inns_item.isCurrentInning.toLowerCase() == 'yes') {
					current_inning = index;
				}
			});
			
			document.getElementById('select_match_innings_div').style.display = '';
			document.getElementById('inning_div').style.display = 'none';
			document.getElementById('select_event_div').style.display = 'none';
       		//document.getElementById('load_historic_match_div').style.display = 'none';
			document.getElementById('match_data_update_div').style.display = 'none';
			document.getElementById('select_match_status_div').style.display = 'none';
			document.getElementById('start_pause_match_time_div').style.display = 'none';
			document.getElementById('select_day_session_div').style.display = 'none';
			document.getElementById('isDeclared_div').style.display = 'none';
			
			if(current_inning >= 0) {
				document.getElementById('select_match_innings').selectedIndex = current_inning;
				addItemsToList('LOAD_MATCH',dataToProcess);
				document.getElementById('inning_div').style.display = '';
				document.getElementById('match_data_update_div').style.display = '';
				document.getElementById('select_match_status_div').style.display = '';
				document.getElementById('start_pause_match_time_div').style.display = '';
				if(dataToProcess.setup.matchType.toUpperCase() == 'TEST' || dataToProcess.setup.matchType.toUpperCase() == 'FC') {
					document.getElementById('select_day_session_div').style.display = '';
					document.getElementById('isDeclared_div').style.display = '';
					dataToProcess.match.inning.forEach(function(inns_item,index,arr){
						if(document.getElementById('select_match_innings').value == inns_item.inningNumber) {
							if(inns_item.isDeclared) {
								document.getElementById('isDeclared').value = inns_item.isDeclared;
							} else {
								document.getElementById('isDeclared').selectedIndex = 0;
							}
						}
					});
					if(dataToProcess.match.daysSessions != null) {
						dataToProcess.match.daysSessions.forEach(function(ds,index,arr){
							if(ds.isCurrentSession != null && ds.isCurrentSession.toUpperCase() == 'YES') {
								document.getElementById('selected_day_session').innerHTML = 'Selected day = ' + ds.dayNumber 
									+ ' & selected session = ' + ds.sessionNumber;
								document.getElementById('select_day').value = ds.dayNumber;
								document.getElementById('select_session').value = ds.sessionNumber;
							}
						});
					}
				}
			}
		}

		break;
	
	case 'LOAD_UNDO':

		$('#extra_log_event_row_1').empty();
		
		if(dataToProcess.eventFile.events.length > 0) {
			
			select = document.createElement('select');
			select.id = 'select_undo';
			dataToProcess.eventFile.events = dataToProcess.eventFile.events.reverse();
			var max_loop = dataToProcess.eventFile.events.length;
			if(max_loop > 5) {
				max_loop = 5;
			}
			for(var i = 0; i < max_loop; i++) {
				if(dataToProcess.eventFile.events[i].eventType.toUpperCase() != 'SHOT' 
						&& dataToProcess.eventFile.events[i].eventType.toUpperCase() != 'WAGON') {
					option = document.createElement('option');
					option.value = dataToProcess.eventFile.events[i].eventNumber;
				    option.text = dataToProcess.eventFile.events[i].eventNumber + '. ' + getFullEventTypeWord(dataToProcess.eventFile.events[i].eventType);
				    select.appendChild(option);
				}
			}
			header_text = document.createElement('label');
			header_text.innerHTML = 'Last 5 ';
			header_text.htmlFor = select.id;
			document.getElementById('extra_log_event_row_1').insertCell(0).appendChild(header_text).appendChild(select);
			//$('#' + select.id).select2({dropdownAutoWidth : true});

		    option = document.createElement('input');
		    option.type = 'text';
		    option.name = 'number_of_undo_txt';
		    option.value = '1';
		    option.id = option.name;
		    option.size = 3;
		    option.maxLength = 3;
		    option.setAttribute('onblur','processUserSelection(this)');
			header_text = document.createElement('label');
			header_text.innerHTML = 'Undos Count ';
			header_text.htmlFor = option.id;
			document.getElementById('extra_log_event_row_1').insertCell(1).appendChild(header_text).appendChild(option);
			
		    div = document.createElement('div');

		    option = document.createElement('input');
		    option.type = 'button';
		    option.name = 'log_undo_btn';
		    option.id = option.name;
		    option.value = 'Undo Last Event';
		    option.setAttribute('onclick','processUserSelection(this);');
		    
		    div.append(option);

			option = document.createElement('input');
			option.type = 'button';
			option.name = 'cancel_undo_btn';
			option.id = option.name;
			option.value = 'Cancel';
			option.style.marginTop = '8px'; 
			option.setAttribute('onclick','processUserSelection(this)');

		    div.append(document.createElement('br'));
		    div.append(option);

		    document.getElementById('extra_log_event_row_1').insertCell(2).appendChild(div);

		} else {
			return false;
		}
		break;

	case 'LOAD_FINISH':
		
		$('#extra_log_event_row_1').empty();

	    option = document.createElement('input');
	    option.type = 'text';
	    option.name = 'start_of_play_txt';
	    option.value = '9:30';
	    option.id = option.name;
		option.size = '2';
		header_text = document.createElement('label');
		header_text.innerHTML = 'Start of play: ';
		header_text.htmlFor = option.id;
		document.getElementById('extra_log_event_row_1').insertCell(0).appendChild(header_text).appendChild(option);

	    option = document.createElement('input');
	    option.type = 'text';
	    option.name = 'start_of_lunch_txt';
	    option.value = '11:30';
	    option.id = option.name;
		option.size = '2';
		header_text = document.createElement('label');
		header_text.innerHTML = 'Lunch Start: ';
		header_text.htmlFor = option.id;
		document.getElementById('extra_log_event_row_1').insertCell(1).appendChild(header_text).appendChild(option);

	    option = document.createElement('input');
	    option.type = 'text';
	    option.name = 'end_of_lunch_txt';
	    option.value = '12:10';
	    option.id = option.name;
		option.size = '2';
		header_text = document.createElement('label');
		header_text.innerHTML = 'Lunch End: ';
		header_text.htmlFor = option.id;
		document.getElementById('extra_log_event_row_1').insertCell(2).appendChild(header_text).appendChild(option);

	    option = document.createElement('input');
	    option.type = 'text';
	    option.name = 'start_of_tea_txt';
	    option.value = '14:10';
	    option.id = option.name;
		option.size = '2';
		header_text = document.createElement('label');
		header_text.innerHTML = 'Tea Start: ';
		header_text.htmlFor = option.id;
		document.getElementById('extra_log_event_row_1').insertCell(3).appendChild(header_text).appendChild(option);

	    option = document.createElement('input');
	    option.type = 'text';
	    option.name = 'end_of_tea_txt';
	    option.value = '14:30';
	    option.id = option.name;
		option.size = '2';
		header_text = document.createElement('label');
		header_text.innerHTML = 'Tea End: ';
		header_text.htmlFor = option.id;
		document.getElementById('extra_log_event_row_1').insertCell(4).appendChild(header_text).appendChild(option);
		
	    option = document.createElement('input');
	    option.type = 'text';
	    option.name = 'end_of_play_txt';
	    option.value = '16:30';
	    option.id = option.name;
		option.size = '2';
		header_text = document.createElement('label');
		header_text.innerHTML = 'Close Of Play: ';
		header_text.htmlFor = option.id;
		document.getElementById('extra_log_event_row_1').insertCell(5).appendChild(header_text).appendChild(option);

		option = document.createElement('input');
		option.type = 'text';
		option.name = 'max_overs_txt';
		option.value = '90';
		option.id = option.name;
		option.size = '2';
		header_text = document.createElement('label');
		header_text.innerHTML = 'Max Overs: ';
		header_text.htmlFor = option.id;
		document.getElementById('extra_log_event_row_1').insertCell(6).appendChild(header_text).appendChild(option);

		option = document.createElement('input');
		option.type = 'text';
		option.name = 'new_ball_overs_txt';
		option.value = '90';
		option.id = option.name;
		option.size = '2';
		header_text = document.createElement('label');
		header_text.innerHTML = 'New Ball Overs: ';
		header_text.htmlFor = option.id;
		document.getElementById('extra_log_event_row_1').insertCell(7).appendChild(header_text).appendChild(option);
		
	    div = document.createElement('div');

	    option = document.createElement('input');
	    option.type = 'button';
	    option.name = 'log_finish_btn';
	    option.id = option.name;
	    option.value = 'Log Finish';
	    option.setAttribute('onclick','processUserSelection(this);');
	    
	    div.append(option);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'cancel_finish_btn';
		option.id = option.name;
		option.value = 'Cancel';
		option.style.marginTop = '8px'; 
		option.setAttribute('onclick','processUserSelection(this)');

	    div.append(document.createElement('br'));
	    div.append(option);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'log_new_ball_btn';
		option.id = option.name;
		option.value = 'Log New Ball';
		option.setAttribute('onclick','processUserSelection(this)');

		div.append(document.createElement('br'));
		div.append(option);
		
	    document.getElementById('extra_log_event_row_1').insertCell(8).appendChild(div);
		break;

	case 'LOAD_IMPACT':
		
		$('#extra_log_event_row_1').empty();

		select = document.createElement('select');
		select.id = 'select_impact_inning';
		for(var i = 1; i <= 2; i++) {
			option = document.createElement('option');
			option.value = i;
		    option.text = i;
		    select.appendChild(option);
		}
		select.name = select.id;
		header_text = document.createElement('label');
		header_text.innerHTML = 'Inning ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(0).appendChild(header_text).appendChild(select);

		select = document.createElement('select');
		select.id = 'select_impact_team';
		match_data.match.inning.forEach(function(inn,index,arr) {
			switch(index) {
			case 0: case 1:
				option = document.createElement('option');
				option.value = inn.battingTeamId;
			    option.text = inn.batting_team.teamName1;
				if(inn.isCurrentInning.toLowerCase() == 'yes') {
					option.selected = true;
				}
			    select.appendChild(option);
				break;
			}
		});			
		select.name = select.id;
	    select.setAttribute('onchange','processUserSelection(this)');
		header_text = document.createElement('label');
		header_text.innerHTML = 'Team ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(1).appendChild(header_text).appendChild(select);

		for(var iPlay = 1; iPlay <= 2; iPlay++) {
			header_text = document.createElement('label');
			select = document.createElement('select');
			select.style = 'width:75%';
			if(iPlay==1) {
				header_text.innerHTML = 'Out ';
				select.name = 'select_impact_outgoing_player';
			}else {
				header_text.innerHTML = 'In ';
				select.name = 'select_impact_incoming_player';
			}
			select.id = select.name;
			header_text.htmlFor = select.id;
			document.getElementById('extra_log_event_row_1').insertCell(iPlay+1).appendChild(header_text).appendChild(select);
			removeDuplicateOptions(select.id);			
		}
		processUserSelection($('#select_impact_team'));
		max_cols = 3;

		select = document.createElement('select');
		select.id = 'select_impact_or_concussion';
		for(var i=1;i<=2;i++) {
			option = document.createElement('option');
			switch(i) {
			case 1:
				option.value = 'impact';
				option.text = 'Impact';
				break;
			case 2:
				option.value = 'concussed';
				option.text = 'Concussed';
				break;
			}
		    select.appendChild(option);
		}
		header_text = document.createElement('label');
		header_text.innerHTML = 'Impact/Concusd ';
		header_text.htmlFor = select.id;
		max_cols = max_cols + 1;
		document.getElementById('extra_log_event_row_1').insertCell(max_cols).appendChild(header_text).appendChild(select);
		
		if(match_data.eventFile.events.length > 0) {
			
			match_data.match.inning.forEach(function(inn,index,arr){
				
				if(inn.isCurrentInning.toLowerCase() == 'yes') {

					select = document.createElement('select');
					select.id = 'select_impact_undo';
					for(var i = 0; i < match_data.eventFile.events.length; i++) {
						if(match_data.eventFile.events[i].eventType.toUpperCase() === 'LOG_IMPACT' 
							&& match_data.eventFile.events[i].eventInningNumber == inn.inningNumber 
							&& match_data.eventFile.events[i].eventBattingCard != null) {
							option = document.createElement('option');
							option.value = match_data.eventFile.events[i].eventNumber;
						    option.text = match_data.eventFile.events[i].eventBattingCard.player.full_name; 
						    select.appendChild(option);
						}
					}
					
					if(select.options.length > 0) {
						
						header_text = document.createElement('label');
						header_text.innerHTML = 'UNDO Impacts: ';
						header_text.htmlFor = select.id;
						max_cols = max_cols + 1;
						document.getElementById('extra_log_event_row_1').insertCell(max_cols).appendChild(header_text).appendChild(select);

						option = document.createElement('input');
						option.type = 'button';
						option.name = 'log_undo_impact_btn';
						option.id = option.name;
						option.value = 'Undo Impact';
						option.setAttribute('onclick','processUserSelection(this)');
						max_cols = max_cols + 1;
						document.getElementById('extra_log_event_row_1').insertCell(max_cols).appendChild(option);
					}
				}
			});
		}
		
	    div = document.createElement('div');

	    option = document.createElement('input');
	    option.type = 'button';
	    option.name = 'log_impact_btn';
	    option.id = option.name;
	    option.value = 'Log Impact';
	    option.setAttribute('onclick','processUserSelection(this)');
	    
	    div.append(option);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'cancel_review_btn';
		option.id = option.name;
		option.value = 'Cancel';
		option.style.marginTop = '8px'; 
		option.setAttribute('onclick','processUserSelection(this)');

	    div.append(document.createElement('br'));
	    div.append(option);
		max_cols = max_cols + 1;
	    document.getElementById('extra_log_event_row_1').insertCell(max_cols).appendChild(div);

		break;
		
	case 'LOAD_REVIEW':
		
		$('#extra_log_event_row_1').empty();

		select = document.createElement('select');
		select.id = 'select_review_team';
		for(var iTeam=1; iTeam <= 2; iTeam++) {
			option = document.createElement('option');
			if(iTeam==1) {
				option.value = match_data.setup.homeTeamId;
			    option.text = match_data.setup.homeTeam.teamName4;
			} else {
				option.value = match_data.setup.awayTeamId;
			    option.text = match_data.setup.awayTeam.teamName4;
			}
		    select.appendChild(option);
		}
		header_text = document.createElement('label');
		header_text.innerHTML = 'Team: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(0).appendChild(header_text).appendChild(select);
		
		select = document.createElement('select');
		select.id = 'select_review_result';
		for(var i=1; i <= 2; i++) {
			option = document.createElement('option');
			if(i==1) {
				option.value = 'successful';
			    option.text = 'Pass';
			} else {
				option.value = 'unsuccessful';
			    option.text = 'Fail';
			}
		    select.appendChild(option);
		}
		header_text = document.createElement('label');
		header_text.innerHTML = 'Result: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(1).appendChild(header_text).appendChild(select);

		select = document.createElement('select');
		select.id = 'select_review_retain';
		for(var i=1; i <= 2; i++) {
			option = document.createElement('option');
			if(i==1) {
				option.value = 'retained';
			    option.text = 'Retained';
			} else {
				option.value = 'unretained';
			    option.text = 'Lost';
			}
		    select.appendChild(option);
		}
		header_text = document.createElement('label');
		header_text.innerHTML = 'Retained: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(2).appendChild(header_text).appendChild(select);
		
	    div = document.createElement('div');

	    option = document.createElement('input');
	    option.type = 'button';
	    option.name = 'log_review_btn';
	    option.id = option.name;
	    option.value = 'Log Review';
	    option.setAttribute('onclick','processUserSelection(this)');
	    
	    div.append(option);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'cancel_review_btn';
		option.id = option.name;
		option.value = 'Cancel';
		option.style.marginTop = '8px'; 
		option.setAttribute('onclick','processUserSelection(this)');

	    div.append(option);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'overwrite_review_btn';
		option.id = option.name;
		option.value = 'Overwrite';
		option.style.marginTop = '8px'; 
		option.setAttribute('onclick','processUserSelection(this)');

		div.append(document.createElement('br'));
		div.append(option);
	
	    document.getElementById('extra_log_event_row_1').insertCell(3).appendChild(div);

		header_text = document.createElement('label');
		header_text.innerHTML = ''; 
	    document.getElementById('extra_log_event_row_1').insertCell(4).appendChild(header_text);

		var successful_reviews_1 = 0, unsuccessful_reviews_1 = 0, retained_reviews_1 = 0, unretained_reviews_1 = 0, 
			successful_reviews_2 = 0, unsuccessful_reviews_2 = 0, retained_reviews_2 = 0, unretained_reviews_2 = 0;
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
				if(inn.reviews != null) {
					inn.reviews.forEach(function(rev,index,arr){
						if(rev.reviewTeamId == match_data.setup.homeTeamId) {
							if(rev.reviewRetained.toLowerCase() == 'retained') {
								retained_reviews_1 = retained_reviews_1 + 1;
							} else if(rev.reviewRetained.toLowerCase() == 'unretained') {
								unretained_reviews_1 = unretained_reviews_1 + 1;
							}
							if(rev.reviewStatus.toLowerCase() == 'successful') {
								successful_reviews_1 = successful_reviews_1 + 1;
							} else if(rev.reviewStatus.toLowerCase() == 'unsuccessful') {
								unsuccessful_reviews_1 = unsuccessful_reviews_1 + 1;
							}
						}
						if(rev.reviewTeamId == match_data.setup.awayTeamId) {
							if(rev.reviewRetained.toLowerCase() == 'retained') {
								retained_reviews_2 = retained_reviews_2 + 1;
							} if(rev.reviewRetained.toLowerCase() == 'unretained') {
								unretained_reviews_2 = unretained_reviews_2 + 1;
							}
							if(rev.reviewStatus.toLowerCase() == 'successful') {
								successful_reviews_2 = successful_reviews_2 + 1;
							} else if(rev.reviewStatus.toLowerCase() == 'unsuccessful') {
								unsuccessful_reviews_2 = unsuccessful_reviews_2 + 1;
							}
						}
					});
				}
			}
		});
			
		table = document.createElement('table');
		table.setAttribute('class', 'table table-bordered');
		tr = document.createElement('tr');
		for (var j = 0; j <= 2; j++) {
		    th = document.createElement('th'); //column
		    switch (j) {
			case 0:
			    text = document.createTextNode('Stats'); 
				break;
			case 1:
			    text = document.createTextNode(GetFirstFewChars(match_data.setup.homeTeam.teamName4, 3)); 
				break;
			case 2:
			    text = document.createTextNode(GetFirstFewChars(match_data.setup.awayTeam.teamName4, 3)); 
				break;
			}
		    th.appendChild(text);
		    tr.appendChild(th);
		}
		thead = document.createElement('thead');
		thead.appendChild(tr);
		table.appendChild(thead);		
	
		tbody = document.createElement('tbody');
		table.appendChild(tbody);
		
		for (var iStat = 1; iStat<= 6; iStat++) {	
			row = tbody.insertRow(tbody.rows.length);
		 	switch(iStat){
			case 1:

			 	cell = row.insertCell(0);
				cell.innerHTML = 'Total';
			 	cell = row.insertCell(1);
				cell.innerHTML = match_data.setup.reviewsPerTeam;
			 	cell = row.insertCell(2);
				cell.innerHTML = match_data.setup.reviewsPerTeam;
				break;

			case 2:

			 	cell = row.insertCell(0);
				cell.innerHTML = 'Success';
			
			 	cell = row.insertCell(1);
				cell.innerHTML = successful_reviews_1;
				cell.setAttribute("name", 'overwriteSuccessfullReviews');
				cell.setAttribute("id", 'overwriteSuccessfullReviews_1');
				cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
				cell.addEventListener("contextmenu", function (e) {
				    e.preventDefault(); 
				    processUserSelection(this, "DOUBLE_CLICK");
				});			
				
			 	cell = row.insertCell(2);
				cell.innerHTML = successful_reviews_2;
				cell.setAttribute("name", 'overwriteSuccessfullReviews');
				cell.setAttribute("id", 'overwriteSuccessfullReviews_2');
				cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
				cell.addEventListener("contextmenu", function (e) {
				    e.preventDefault(); 
				    processUserSelection(this, "DOUBLE_CLICK");
				});			

				break;
				
			case 3:
			
			 	cell = row.insertCell(0);
				cell.innerHTML = 'Fail';

			 	cell = row.insertCell(1);
				cell.innerHTML = unsuccessful_reviews_1;
				cell.setAttribute("name", 'overwriteUnsuccessfullReviews');
				cell.setAttribute("id", 'overwriteUnsuccessfullReviews_1');
				cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
				cell.addEventListener("contextmenu", function (e) {
				    e.preventDefault(); 
				    processUserSelection(this, "DOUBLE_CLICK");
				});			
				
			 	cell = row.insertCell(2);
				cell.innerHTML = unsuccessful_reviews_2;
				cell.setAttribute("name", 'overwriteUnsuccessfullReviews');
				cell.setAttribute("id", 'overwriteUnsuccessfullReviews_2');
				cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
				cell.addEventListener("contextmenu", function (e) {
				    e.preventDefault(); 
				    processUserSelection(this, "DOUBLE_CLICK");
				});			
				
				break;
				
			case 4:

			 	cell = row.insertCell(0);
				cell.innerHTML = 'Retain';
			
			 	cell = row.insertCell(1);
				cell.innerHTML = retained_reviews_1;
				cell.setAttribute("name", 'overwriteRetainedReviews');
				cell.setAttribute("id", 'overwriteRetainedReviews_1');
				cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
				cell.addEventListener("contextmenu", function (e) {
				    e.preventDefault(); 
				    processUserSelection(this, "DOUBLE_CLICK");
				});			
				
			 	cell = row.insertCell(2);
				cell.innerHTML = retained_reviews_2;
				cell.setAttribute("name", 'overwriteRetainedReviews');
				cell.setAttribute("id", 'overwriteRetainedReviews_2');
				cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
				cell.addEventListener("contextmenu", function (e) {
				    e.preventDefault(); 
				    processUserSelection(this, "DOUBLE_CLICK");
				});			
				
				break;
				
			case 5:
			
			 	cell = row.insertCell(0);
				cell.innerHTML = 'Lost';

			 	cell = row.insertCell(1);
				cell.innerHTML = unretained_reviews_1;
				cell.setAttribute("name", 'overwriteUnretainedReviews');
				cell.setAttribute("id", 'overwriteUnretainedReviews_1');
				cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
				cell.addEventListener("contextmenu", function (e) {
				    e.preventDefault(); 
				    processUserSelection(this, "DOUBLE_CLICK");
				});			
				
			 	cell = row.insertCell(2);
				cell.innerHTML = unretained_reviews_2;
				cell.setAttribute("name", 'overwriteUnretainedReviews');
				cell.setAttribute("id", 'overwriteUnretainedReviews_2');
				cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
				cell.addEventListener("contextmenu", function (e) {
				    e.preventDefault(); 
				    processUserSelection(this, "DOUBLE_CLICK");
				});			
				
				break;
				
			case 6:
			
			 	cell = row.insertCell(0);
				cell.innerHTML = 'Remain';

			 	cell = row.insertCell(1);
				cell.innerHTML = match_data.setup.reviewsPerTeam - unretained_reviews_1;
				cell.setAttribute("name", 'overwriteReviewsRemaining');
				cell.setAttribute("id", 'overwriteReviewsRemaining_1');
				cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
				cell.addEventListener("contextmenu", function (e) {
				    e.preventDefault(); 
				    processUserSelection(this, "DOUBLE_CLICK");
				});			
				
			 	cell = row.insertCell(2);
				cell.innerHTML = match_data.setup.reviewsPerTeam - unretained_reviews_2;
				cell.setAttribute("name", 'overwriteReviewsRemaining');
				cell.setAttribute("id", 'overwriteReviewsRemaining_2');
				cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
				cell.addEventListener("contextmenu", function (e) {
				    e.preventDefault(); 
				    processUserSelection(this, "DOUBLE_CLICK");
				});			
				
				break;
			}
		}	
		
		div = document.createElement('div');
		div.append(table);
		document.getElementById('extra_log_event_row_1').cells[4].appendChild(div);
		normalizeAndAppendTable(table, div, 'extra_log_event_row_1');

		table.style.tableLayout = 'auto';
		table.style.width = 'auto';
		
/*		table = document.createElement('table');
		table.setAttribute('class', 'table table-bordered events-table');
		tr = document.createElement('tr');
		for(var iTeam=1; iTeam <= 3; iTeam++) {
		    th = document.createElement('th');
		    switch(iTeam) {
			case 1:
				text = document.createTextNode(match_data.setup.homeTeam.teamName4);
				break;
			case 2:
				text = document.createTextNode('Stats');
				break;
			case 3:
				text = document.createTextNode(match_data.setup.awayTeam.teamName4);
				break;
			}
		    th.appendChild(text);
		    tr.appendChild(th);
		}
		thead = document.createElement('thead');
		thead.appendChild(tr);
		table.appendChild(thead);
		
		tbody = document.createElement('tbody');
		table.appendChild(tbody);
		
		for (var iStat = 1; iStat<= 6; iStat++) {
						
			row = tbody.insertRow(tbody.rows.length);
		 	
		 	switch(iStat){
			case 1:
			
			 	cell = row.insertCell(0);
				cell.innerHTML = match_data.setup.reviewsPerTeam;
				
			 	cell = row.insertCell(1);
				cell.innerHTML = 'Total';
				
			 	cell = row.insertCell(2);
				cell.innerHTML = match_data.setup.reviewsPerTeam;
				
				break;
				
			case 2:
			
			 	cell = row.insertCell(0);
				cell.innerHTML = successful_reviews_1;
				cell.setAttribute("name", 'overwriteSuccessfullReviews');
				cell.setAttribute("id", 'overwriteSuccessfullReviews_1');
				cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
				cell.addEventListener("contextmenu", function (e) {
				    e.preventDefault(); 
				    processUserSelection(this, "DOUBLE_CLICK");
				});			
				
			 	cell = row.insertCell(1);
				cell.innerHTML = 'Success';

			 	cell = row.insertCell(2);
				cell.innerHTML = successful_reviews_2;
				cell.setAttribute("name", 'overwriteSuccessfullReviews');
				cell.setAttribute("id", 'overwriteSuccessfullReviews_2');
				cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
				cell.addEventListener("contextmenu", function (e) {
				    e.preventDefault(); 
				    processUserSelection(this, "DOUBLE_CLICK");
				});			

				break;
				
			case 3:
			
			 	cell = row.insertCell(0);
				cell.innerHTML = unsuccessful_reviews_1;
				cell.setAttribute("name", 'overwriteUnsuccessfullReviews');
				cell.setAttribute("id", 'overwriteUnsuccessfullReviews_1');
				cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
				cell.addEventListener("contextmenu", function (e) {
				    e.preventDefault(); 
				    processUserSelection(this, "DOUBLE_CLICK");
				});			
				
			 	cell = row.insertCell(1);
				cell.innerHTML = 'Fail';
				
			 	cell = row.insertCell(2);
				cell.innerHTML = unsuccessful_reviews_2;
				cell.setAttribute("name", 'overwriteUnsuccessfullReviews');
				cell.setAttribute("id", 'overwriteUnsuccessfullReviews_2');
				cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
				cell.addEventListener("contextmenu", function (e) {
				    e.preventDefault(); 
				    processUserSelection(this, "DOUBLE_CLICK");
				});			
				
				break;
				
			case 4:
			
			 	cell = row.insertCell(0);
				cell.innerHTML = retained_reviews_1;
				cell.setAttribute("name", 'overwriteRetainedReviews');
				cell.setAttribute("id", 'overwriteRetainedReviews_1');
				cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
				cell.addEventListener("contextmenu", function (e) {
				    e.preventDefault(); 
				    processUserSelection(this, "DOUBLE_CLICK");
				});			
				
			 	cell = row.insertCell(1);
				cell.innerHTML = 'Retain';
				
			 	cell = row.insertCell(2);
				cell.innerHTML = retained_reviews_2;
				cell.setAttribute("name", 'overwriteRetainedReviews');
				cell.setAttribute("id", 'overwriteRetainedReviews_2');
				cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
				cell.addEventListener("contextmenu", function (e) {
				    e.preventDefault(); 
				    processUserSelection(this, "DOUBLE_CLICK");
				});			
				
				break;
				
			case 5:
			
			 	cell = row.insertCell(0);
				cell.innerHTML = unretained_reviews_1;
				cell.setAttribute("name", 'overwriteUnretainedReviews');
				cell.setAttribute("id", 'overwriteUnretainedReviews_1');
				cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
				cell.addEventListener("contextmenu", function (e) {
				    e.preventDefault(); 
				    processUserSelection(this, "DOUBLE_CLICK");
				});			
				
			 	cell = row.insertCell(1);
				cell.innerHTML = 'Lost';
				
			 	cell = row.insertCell(2);
				cell.innerHTML = unretained_reviews_2;
				cell.setAttribute("name", 'overwriteUnretainedReviews');
				cell.setAttribute("id", 'overwriteUnretainedReviews_2');
				cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
				cell.addEventListener("contextmenu", function (e) {
				    e.preventDefault(); 
				    processUserSelection(this, "DOUBLE_CLICK");
				});			
				
				break;
				
			case 6:
			
			 	cell = row.insertCell(0);
				cell.innerHTML = match_data.setup.reviewsPerTeam - unretained_reviews_1;
				cell.setAttribute("name", 'overwriteReviewsRemaining');
				cell.setAttribute("id", 'overwriteReviewsRemaining_1');
				cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
				cell.addEventListener("contextmenu", function (e) {
				    e.preventDefault(); 
				    processUserSelection(this, "DOUBLE_CLICK");
				});			
				
			 	cell = row.insertCell(1);
				cell.innerHTML = 'Remain';
				
			 	cell = row.insertCell(2);
				cell.innerHTML = match_data.setup.reviewsPerTeam - unretained_reviews_2;
				cell.setAttribute("name", 'overwriteReviewsRemaining');
				cell.setAttribute("id", 'overwriteReviewsRemaining_2');
				cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
				cell.addEventListener("contextmenu", function (e) {
				    e.preventDefault(); 
				    processUserSelection(this, "DOUBLE_CLICK");
				});			
				
				break;
			}
		}
		
	    div = document.createElement('div');
	    div.append(table);
		normalizeAndAppendTable(table, div, 'extra_log_event_row_1');
		
		document.getElementById('extra_log_event_row_1').insertCell(4).appendChild(div);*/
		break;		
				
/*	case 'LOAD_REVIEW':
		
		$('#extra_log_event_row_1').empty();

		select = document.createElement('select');
		select.id = 'select_review_team';
		for(var iTeam=1; iTeam <= 2; iTeam++) {
			option = document.createElement('option');
			if(iTeam==1) {
				option.value = match_data.setup.homeTeamId;
			    option.text = match_data.setup.homeTeam.teamName4;
			} else {
				option.value = match_data.setup.awayTeamId;
			    option.text = match_data.setup.awayTeam.teamName4;
			}
		    select.appendChild(option);
		}
		header_text = document.createElement('label');
		header_text.innerHTML = 'Team: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(0).appendChild(header_text).appendChild(select);
		
		select = document.createElement('select');
		select.id = 'select_review_result';
		for(var i=1; i <= 2; i++) {
			option = document.createElement('option');
			if(i==1) {
				option.value = 'successful';
			    option.text = 'Pass';
			} else {
				option.value = 'unsuccessful';
			    option.text = 'Fail';
			}
		    select.appendChild(option);
		}
		header_text = document.createElement('label');
		header_text.innerHTML = 'Result: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(1).appendChild(header_text).appendChild(select);

		select = document.createElement('select');
		select.id = 'select_review_retain';
		for(var i=1; i <= 2; i++) {
			option = document.createElement('option');
			if(i==1) {
				option.value = 'retained';
			    option.text = 'Retained';
			} else {
				option.value = 'unretained';
			    option.text = 'Lost';
			}
		    select.appendChild(option);
		}
		header_text = document.createElement('label');
		header_text.innerHTML = 'Retained: ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(2).appendChild(header_text).appendChild(select);
		
	    div = document.createElement('div');

	    option = document.createElement('input');
	    option.type = 'button';
	    option.name = 'log_review_btn';
	    option.id = option.name;
	    option.value = 'Log Review';
	    option.setAttribute('onclick','processUserSelection(this)');
	    
	    div.append(option);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'cancel_review_btn';
		option.id = option.name;
		option.value = 'Cancel';
		option.style.marginTop = '8px'; 
		option.setAttribute('onclick','processUserSelection(this)');

	    div.append(document.createElement('br'));
	    div.append(option);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'overwrite_review_btn';
		option.id = option.name;
		option.value = 'Overwrite';
		option.setAttribute('onclick','processUserSelection(this)');

		div.append(document.createElement('br'));
		div.append(option);
	
	    document.getElementById('extra_log_event_row_1').insertCell(3).appendChild(div);

		table = document.createElement('table');
		table.setAttribute('class', 'table table-bordered');
		tr = document.createElement('tr');
		for (var j = 0; j <= 6; j++) {
		    th = document.createElement('th'); //column
		    switch (j) {
			case 0:
			    text = document.createTextNode('Teams'); 
				break;
			case 1:
			    text = document.createTextNode('Total'); 
				break;
			case 2:
			    text = document.createTextNode('Success'); 
				break;
			case 3:
			    text = document.createTextNode('Fail'); 
				break;
			case 4:
			    text = document.createTextNode('Retained'); 
				break;
			case 5:
			    text = document.createTextNode('Lost'); 
				break;
			case 6:
			    text = document.createTextNode('Remaining'); 
				break;
			}
		    th.appendChild(text);
		    tr.appendChild(th);
		}
		thead = document.createElement('thead');
		thead.appendChild(tr);
		table.appendChild(thead);
		
		tbody = document.createElement('tbody');
		table.appendChild(tbody);

		var successful_reviews = 0, unsuccessful_reviews = 0, retained_reviews = 0, unretained_reviews = 0;
		for(var iTeam=1; iTeam <= 2; iTeam++) {
			
			row = tbody.insertRow(tbody.rows.length);
		 	cell = row.insertCell(0);

			if(iTeam==1) {
				cell.innerHTML = match_data.setup.homeTeam.teamName4;
			} else if(iTeam==2) {
				cell.innerHTML = match_data.setup.awayTeam.teamName4;
			}

			cell = row.insertCell(1);
			cell.innerHTML = match_data.setup.reviewsPerTeam;

			successful_reviews = 0, unsuccessful_reviews = 0, retained_reviews = 0, unretained_reviews = 0;
			match_data.match.inning.forEach(function(inn,index,arr){
				if(inn.isCurrentInning.toLowerCase() == 'yes') {
					if(inn.reviews) {
						inn.reviews.forEach(function(rev,index,arr){
							if(iTeam==1) {
								if(rev.reviewTeamId == match_data.setup.homeTeamId) {
									if(rev.reviewRetained.toLowerCase() == 'retained') {
										retained_reviews = retained_reviews + 1;
									} else if(rev.reviewRetained.toLowerCase() == 'unretained') {
										unretained_reviews = unretained_reviews + 1;
									}
									if(rev.reviewStatus.toLowerCase() == 'successful') {
										successful_reviews = successful_reviews + 1;
									} else if(rev.reviewStatus.toLowerCase() == 'unsuccessful') {
										unsuccessful_reviews = unsuccessful_reviews + 1;
									}
								}
							} else if(iTeam==2) {
								if(rev.reviewTeamId == match_data.setup.awayTeamId) {
									if(rev.reviewRetained.toLowerCase() == 'retained') {
										retained_reviews = retained_reviews + 1;
									} if(rev.reviewRetained.toLowerCase() == 'unretained') {
										unretained_reviews = unretained_reviews + 1;
									}
									if(rev.reviewStatus.toLowerCase() == 'successful') {
										successful_reviews = successful_reviews + 1;
									} else if(rev.reviewStatus.toLowerCase() == 'unsuccessful') {
										unsuccessful_reviews = unsuccessful_reviews + 1;
									}
								}
							}
						});
					}
				}
			});
			cell = row.insertCell(2);
			cell.innerHTML = successful_reviews;
			cell.setAttribute("name", 'overwriteSuccessfullReviews');
			cell.setAttribute("id", 'overwriteSuccessfullReviews_' + iTeam);
			cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
			cell.addEventListener("contextmenu", function (e) {
			    e.preventDefault(); // stop default right-click menu
			    processUserSelection(this, "DOUBLE_CLICK");
			});			

			cell = row.insertCell(3);
			cell.innerHTML = unsuccessful_reviews;
			cell.setAttribute("name", 'overwriteUnsuccessfullReviews');
			cell.setAttribute("id", 'overwriteUnsuccessfullReviews_' + iTeam);
			cell.setAttribute('onclick','processUserSelection(this, "SINGLE_CLICK")');
			cell.addEventListener("contextmenu", function (e) {
			    e.preventDefault(); // stop default right-click menu
			    processUserSelection(this, "DOUBLE_CLICK");
			});			

			cell = row.insertCell(4);
			cell.innerHTML = retained_reviews;
			cell.setAttribute("name", 'overwriteRetainedReviews');
			cell.setAttribute("id", 'overwriteRetainedReviews_' + iTeam);
			
			cell = row.insertCell(5);
			cell.innerHTML = unretained_reviews;
			cell.setAttribute("name", 'overwriteUnretainedReviews');
			cell.setAttribute("id", 'overwriteUnretainedReviews_' + iTeam);
			
			cell = row.insertCell(6);
			cell.innerHTML = match_data.setup.reviewsPerTeam - unretained_reviews;
			cell.setAttribute("name", 'overwriteReviewsRemaining');
			cell.setAttribute("id", 'overwriteReviewsRemaining_' + iTeam);
			
		}
	    div = document.createElement('div');
	    div.append(table);
	    
		document.getElementById('extra_log_event_row_1').insertCell(0).appendChild(div);

		break;

	case 'LOAD_NEW_BATSMAN':
		
		$('#extra_log_event_row_1').empty();
		
		match_data.match.inning.forEach(function(inn,index,arr) {
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
				
				select = document.createElement('select');
				select.id = 'select_new_batsman';
				select.name = select.id;
				if(inn.battingTeamId == match_data.setup.homeTeamId) {
					match_data.setup.homeSquad.forEach(function(hs,bc_index,bc_arr){
						option = document.createElement('option');
						option.value = hs.playerId;
					    option.text = hs.ticker_name;
					    select.appendChild(option);
					});
					match_data.setup.homeSubstitutes.forEach(function(hs,bc_index,bc_arr){
						option = document.createElement('option');
						option.value = hs.playerId;
					    option.text = hs.ticker_name;
					    select.appendChild(option);
					});
					match_data.setup.homeOtherSquad.forEach(function(hs,bc_index,bc_arr){
						option = document.createElement('option');
						option.value = hs.playerId;
					    option.text = hs.ticker_name;
					    select.appendChild(option);
					});
				}else if(inn.battingTeamId == match_data.setup.awayTeamId) {
					match_data.setup.awaySquad.forEach(function(as,bc_index,bc_arr){
						option = document.createElement('option');
						option.value = as.playerId;
					    option.text = as.ticker_name;
					    select.appendChild(option);
					});
					match_data.setup.awaySubstitutes.forEach(function(as,bc_index,bc_arr){
						option = document.createElement('option');
						option.value = as.playerId;
					    option.text = as.ticker_name;
					    select.appendChild(option);
					});
					match_data.setup.awayOtherSquad.forEach(function(as,bc_index,bc_arr){
						option = document.createElement('option');
						option.value = as.playerId;
					    option.text = as.ticker_name;
					    select.appendChild(option);
					});
				}
			    select.setAttribute('onchange','processUserSelection(this);');

				header_text = document.createElement('label');
				header_text.innerHTML = 'Choose Batsman: ';
				header_text.htmlFor = select.id;
				document.getElementById('extra_log_event_row_1').insertCell(0).appendChild(header_text).appendChild(select);
				removeDuplicateOptions(select.id);

				select = document.createElement('select');
				select.id = 'select_outgoing_batsman';
				select.name = select.id;
				inn.battingCard.forEach(function(bc,index,arr){
					option = document.createElement('option');
					option.value = bc.playerId;
				    option.text = bc.player.ticker_name;
				    select.appendChild(option);
				});
				header_text = document.createElement('label');
				header_text.innerHTML = 'Choose Outgoing Batsman: ';
				header_text.htmlFor = select.id;
				document.getElementById('extra_log_event_row_1').insertCell(1).appendChild(header_text).appendChild(select);

				select = document.createElement('select');
				select.id = 'select_new_batsman_reason';
				for(var i = 1; i <= 3; i++) {
					option = document.createElement('option');
					switch(i) {
					case 1:
						option.value = '';
					    option.text = '';
						break;
					case 2:
						option.value = 'impact';
					    option.text = 'Impact Player';
						break;
					case 3:
						option.value = 'concussion';
					    option.text = 'Concussion';
						break;
					}
				    select.appendChild(option);
				}
				header_text = document.createElement('label');
				header_text.innerHTML = 'Choose Replacement Reason: ';
				header_text.htmlFor = select.id;
				document.getElementById('extra_log_event_row_1').insertCell(2).appendChild(header_text).appendChild(select);	

			}
		});		

		div = document.createElement('div');

	    option = document.createElement('input');
	    option.type = 'button';
	    option.name = 'log_new_batsman_btn';
	    option.id = option.name;
	    option.value = 'Log New Batsman';
	    option.setAttribute('onclick','processCricketProcedures("NEW_BATSMAN",this);');
	    
	    div.append(option);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'cancel_new_batsman_btn';
		option.id = option.name;
		option.value = 'Cancel';
		option.style.marginTop = '8px'; 
		option.setAttribute('onclick','processUserSelection(this)');

	    div.append(document.createElement('br'));
	    div.append(option);

	    document.getElementById('extra_log_event_row_1').insertCell(3).appendChild(div);
		
		break;*/
		
	case 'LOAD_CHANGE_BOWLER':
		
		var last_bowl_end = -1;
		
		$('#extra_log_event_row_1').empty();

		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
				
				select = document.createElement('select');
				select.id = 'select_change_bowler';
				select.name = select.id;
				
				if(inn.bowlingTeamId == match_data.setup.homeTeamId) {
					match_data.setup.homeSquad.forEach(function(hs,bc_index,bc_arr){
						option = document.createElement('option');
						option.value = hs.playerId;
					    option.text = hs.ticker_name;
					    if(bc_index == 10) {
							option.selected = true;
						}
					    select.appendChild(option);
					});
					if(match_data.setup.homeSubstitutes != null) {
						match_data.setup.homeSubstitutes.forEach(function(hs,bc_index,bc_arr){
							option = document.createElement('option');
							option.value = hs.playerId;
							option.text = hs.ticker_name;
						    select.appendChild(option);
						});
					}
					if(match_data.setup.homeOtherSquad != null) {
						match_data.setup.homeOtherSquad.forEach(function(hs,bc_index,bc_arr){
							option = document.createElement('option');
							option.value = hs.playerId;
							option.text = hs.ticker_name;
						    select.appendChild(option);
						});
					}
				}else if(inn.bowlingTeamId == match_data.setup.awayTeamId) {
					match_data.setup.awaySquad.forEach(function(as,as_index,bc_arr){
						option = document.createElement('option');
						option.value = as.playerId;
						option.text = as.ticker_name;
					    if(as_index == 10) {
							option.selected = true;
						}
					    select.appendChild(option);
					});
					if(match_data.setup.awaySubstitutes != null) {
						match_data.setup.awaySubstitutes.forEach(function(as,bc_index,bc_arr){
							option = document.createElement('option');
							option.value = as.playerId;
							option.text = as.ticker_name;
						    select.appendChild(option);
						});
					}
					if(match_data.setup.awayOtherSquad != null) {
						match_data.setup.awayOtherSquad.forEach(function(as,bc_index,bc_arr){
							option = document.createElement('option');
							option.value = as.playerId;
							option.text = as.ticker_name;
						    select.appendChild(option);
						});
					}
				}
			    select.setAttribute('onchange','processUserSelection(this);');

				header_text = document.createElement('label');
				header_text.innerHTML = 'Bowler ';
				header_text.htmlFor = select.id;
				document.getElementById('extra_log_event_row_1').insertCell(0).appendChild(header_text).appendChild(select);
				removeDuplicateOptions(select.id);
				
				plyr = 0;
				for (var i = match_data.eventFile.events.length - 1; i >= 0; i--) {
				  if (match_data.eventFile.events[i].eventType.toLowerCase() == 'change_bowler' 
			  		&& match_data.eventFile.events[i].eventInningNumber == inn.inningNumber) {
					if(last_bowl_end > 0) {
						plyr = match_data.eventFile.events[i].eventBowlerNo;
				    	break;
					} else {
				    	last_bowl_end = match_data.eventFile.events[i].eventBowlingEnd;
					}
				  }
				}
				row = 0;
				$('#' + select.id + ' > option').each(function() {
					row = row + 1;
				    $(this).text(row + '. ' + $(this).text().replace(/^\d+\.\s*/, ''));
					if(plyr > 0 && plyr == $(this).val()) {
						$(this).prop('selected', true);
					}
				});
			}
		});

		select = document.createElement('select');
		select.id = 'select_bowling_end';
		if(match_data.setup.ground != null) {
			for(var i=0;i<=1;i++) {
				option = document.createElement('option');
				if(i==0) {
					option.value = 1;
					option.text = match_data.setup.ground.first_bowling_end;
				} else {
					option.value = 2;
					option.text = match_data.setup.ground.second_bowling_end;
				}
				if(last_bowl_end > 0 && last_bowl_end != option.value) {
					option.selected = true;
				}
			    select.appendChild(option);
			}
		}
		header_text = document.createElement('label');
		header_text.innerHTML = 'Bowling End ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(1).appendChild(header_text).appendChild(select);			
	
		select = document.createElement('select');
		select.id = 'select_bowling_spell';
		select.name = select.id;
		for(var i=1;i<=10;i++) {
			option = document.createElement('option');
			option.value = i;
			option.text = 'Spell ' + i;
		    select.appendChild(option);
		}

		header_text = document.createElement('label');
		header_text.innerHTML = 'Spell ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(2).appendChild(header_text).appendChild(select);
		processUserSelection($('#select_change_bowler'));
		
		max_cols = 3;
		if(match_data.setup.specialMatchRules != null && match_data.setup.specialMatchRules == 'ISPL'){
			
			select = document.createElement('select');
			select.id = 'select_bowling_ball_type';
			select.name = select.id;
			for(var i=1;i<=3;i++) {
				option = document.createElement('option');
				switch(i){
				case 1:
					option.value = 'normal';
					option.text = 'Normal';
					break;
				case 2:
					option.value = 'tape';
					option.text = 'Tape Ball';
					break;
				case 3:
					option.value = 'challenge';
					option.text = 'Challenge Over';
					break;
				}
			    select.appendChild(option);
			}
			header_text = document.createElement('label');
			header_text.innerHTML = 'Ball Type ';
			header_text.htmlFor = select.id;
			document.getElementById('extra_log_event_row_1').insertCell(max_cols).appendChild(header_text).appendChild(select);
			max_cols = max_cols + 1;
			
			select = document.createElement('select');
			select.id = 'select_50_50_challenge_runs';
			for(var i=1; i<=100; i++) {
				option = document.createElement('option');
				option.value = i;
			    option.text = i;
				if(i == 10) {
				    option.selected = true;
				}
			    select.appendChild(option);
			}
			select.setAttribute('onchange','processUserSelection(this);');
			header_text = document.createElement('label');
			header_text.innerHTML = 'Challenge Runs ';
			header_text.htmlFor = select.id;
			document.getElementById('extra_log_event_row_1').insertCell(max_cols).appendChild(header_text).appendChild(select);	
			max_cols = max_cols + 1;
			
		}

		div = document.createElement('div');

	    option = document.createElement('input');
	    option.type = 'button';
	    option.name = 'log_change_bowler_btn';
	    option.id = option.name;
	    option.value = 'Log Bowler';
	    option.setAttribute('onclick','processCricketProcedures("CHANGE_BOWLER",this);');
	    
	    div.append(option);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'cancel_change_bowler_btn';
		option.id = option.name;
		option.value = 'Cancel';
		option.style.marginTop = '8px'; 
		option.setAttribute('onclick','processUserSelection(this)');

	    div.append(document.createElement('br'));
	    div.append(option);

	    document.getElementById('extra_log_event_row_1').insertCell(max_cols).appendChild(div);
		
		break;
		
	case 'LOAD_HOW_OUT': 

		select = document.createElement('select');
		select.id = 'select_how_out';
		for(var i=0;i<=16;i++) {
			option = document.createElement('option');
			switch (i) {
			case 0:
			    option.value = '';
				break;
			case 1:
			    option.value = 'caught';
				break;
			case 2:
			    option.value = 'caught_and_bowled';
				break;
			case 3:
			    option.value = 'bowled';
				break;
			case 4:
			    option.value = 'lbw';
				break;
			case 5:
			    option.value = 'stumped';
				break;
			case 6:
			    option.value = 'run_out';
				break;
			case 7:
			    option.value = 'hit_wicket';
				break;
			case 8:
			    option.value = 'handled_the_ball';
				break;
			case 9:
			    option.value = 'hit_ball_twice';
				break;
			case 10:
			    option.value = 'obstructing_fielder';
				break;
			case 11:
			    option.value = 'timed_out';
				break;
			case 12:
			    option.value = 'retired_hurt';
				break;
			case 13:
			    option.value = 'mankad';
				break;
			case 14:
			    option.value = 'absent_hurt';
				break;
			case 15:
			    option.value = 'concussed';
				break;
			case 16:
			    option.value = 'retired_out';
				break;
			}
		    option.text = option.value.replace(/(^\w{1})|(\s{1}\w{1})/g, match => match.toUpperCase()).replaceAll('_',' ');
		    select.appendChild(option);
		}
		
		header_text = document.createElement('label');
		header_text.innerHTML = 'How out ';
		header_text.htmlFor = select.id;
	    
		$('#extra_log_event_row_1').empty();
		document.getElementById('extra_log_event_row_1').insertCell(0).appendChild(header_text).appendChild(select);
		//$('#' + select.id).select2({dropdownAutoWidth : true});
		
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
				
				select = document.createElement('select');
				select.id = 'select_out_batsman';
				inn.battingCard.forEach(function(bc,index,arr){
					if(bc.status.toLowerCase() == 'not out') {
						option = document.createElement('option');
						option.value = bc.player.playerId;
					    option.text = bc.player.ticker_name;
					    if(bc.onStrike.toLowerCase() == 'yes') {
						    option.selected = true;
					    }
					    select.appendChild(option);
					}
				});
				header_text = document.createElement('label');
				header_text.innerHTML = 'Out batsman ';
				header_text.htmlFor = select.id;
				document.getElementById('extra_log_event_row_1').insertCell(1).appendChild(header_text).appendChild(select);

				select = document.createElement('select');
				select.id = 'select_concussion_replacement_player';
				option = document.createElement('option');
				option.value = '';
			    option.text = '';
			    select.appendChild(option);
				if(inn.battingTeamId == match_data.setup.homeTeamId && match_data.setup.homeOtherSquad != null) {
					match_data.setup.homeOtherSquad.forEach(function(hos,index,arr){
						option = document.createElement('option');
						option.value = hos.playerId;
					    option.text = hos.ticker_name;
					    select.appendChild(option);
					});
				} else if(inn.battingTeamId == match_data.setup.awayTeamId && match_data.setup.awayOtherSquad != null) {
					match_data.setup.awayOtherSquad.forEach(function(hos,index,arr){
						option = document.createElement('option');
						option.value = hos.playerId;
					    option.text = hos.ticker_name;
					    select.appendChild(option);
					});
				}
				header_text = document.createElement('label');
				header_text.innerHTML = 'Replacement batsman ';
				header_text.htmlFor = select.id;
				document.getElementById('extra_log_event_row_1').insertCell(2).appendChild(header_text).appendChild(select);

				document.getElementById('extra_log_event_row_1').cells[2].style.display = 'none';

				select = document.createElement('select');
				select.id = 'select_out_fielder';
				switch(index) {
				case 0: case 1:
					which_inn = 1 - index;
					break;
				case 2: 
					if(match_data.match.inning[1].battingTeamId == match_data.match.inning[2].battingTeamId) {
						which_inn = 0;
					} else {
						which_inn = index - 1;
					}
					break;
				case 3:
					which_inn = index - 1;
					break;
				}
				match_data.match.inning[which_inn].battingCard.forEach(function(bc,bc_index,bc_arr){
					option = document.createElement('option');
					option.value = bc.player.playerId;
				    option.text = bc.player.ticker_name;
					if(bc.player.captainWicketKeeper.toLowerCase().includes('wicket_keeper')) {
						option.selected = true;
					}
				    select.appendChild(option);
				});
				if(match_data.match.inning[which_inn].battingTeamId == match_data.setup.homeTeamId) {
					match_data.setup.homeSquad.forEach(function(hs,bc_index,bc_arr){
						option = document.createElement('option');
						option.value = hs.playerId;
					    option.text = hs.ticker_name;
					    select.appendChild(option);
					});
					if(match_data.setup.homeSubstitutes != null) {
						match_data.setup.homeSubstitutes.sort(
							(a, b) => a.playerPosition - b.playerPosition).forEach(function(hs,bc_index,bc_arr){
							option = document.createElement('option');
							option.value = hs.playerId;
						    option.text = hs.ticker_name;
						    select.appendChild(option);
						});
					}
					if(match_data.setup.homeOtherSquad != null) {
						match_data.setup.homeOtherSquad.forEach(function(hs,bc_index,bc_arr){
							option = document.createElement('option');
							option.value = hs.playerId;
						    option.text = hs.ticker_name;
						    select.appendChild(option);
						});
					}
				}else if(match_data.match.inning[which_inn].battingTeamId == match_data.setup.awayTeamId) {
					match_data.setup.awaySquad.forEach(function(as,bc_index,bc_arr){
						option = document.createElement('option');
						option.value = as.playerId;
					    option.text = as.ticker_name;
					    select.appendChild(option);
					});
					if(match_data.setup.awaySubstitutes != null) {
						match_data.setup.awaySubstitutes.sort(
							(a, b) => a.playerPosition - b.playerPosition).forEach(function(as,bc_index,bc_arr){
							option = document.createElement('option');
							option.value = as.playerId;
						    option.text = as.ticker_name;
						    select.appendChild(option);
						});
					}
					if(match_data.setup.awayOtherSquad != null) {
						match_data.setup.awayOtherSquad.forEach(function(as,bc_index,bc_arr){
							option = document.createElement('option');
							option.value = as.playerId;
						    option.text = as.ticker_name;
						    select.appendChild(option);
						});
					}
				}
				//Don't know fielder (substitute)
				option = document.createElement('option');
				option.value = -1;
			    option.text = "Substitute (Don't Know)";
			    select.appendChild(option);
				
				header_text = document.createElement('label');
				header_text.innerHTML = 'Fielder ';
				header_text.htmlFor = select.id;
				document.getElementById('extra_log_event_row_1').insertCell(3).appendChild(header_text).appendChild(select);
				removeDuplicateOptions(select.id);

				$('#' + select.id + ' > option').each(function() {
					if(match_data.match.inning[which_inn].battingTeamId == match_data.setup.homeTeamId) {
						for(var plyr = 0; plyr <= match_data.setup.homeSquad.length -1; plyr++) {
							if(match_data.setup.homeSquad[plyr].captainWicketKeeper.toLowerCase().includes('wicket_keeper')
								&& match_data.setup.homeSquad[plyr].playerId == this.value) {
								this.selected = true;
								return false;
							}
						}
					}else if(match_data.match.inning[which_inn].battingTeamId == match_data.setup.awayTeamId) {
						for(var plyr = 0; plyr <= match_data.setup.awaySquad.length -1; plyr++) {
							if(match_data.setup.awaySquad[plyr].captainWicketKeeper.toLowerCase().includes('wicket_keeper')
								&& match_data.setup.awaySquad[plyr].playerId == this.value) {
								this.selected = true;
								return false;
							}
						}
					}
				});				

				select = document.createElement('select');
				select.id = 'select_how_out_fielder_substitute';
				option = document.createElement('option');
				option.value = 'NO';
			    option.text = 'NO';
			    select.appendChild(option);
				option = document.createElement('option');
				option.value = 'YES';
			    option.text = 'YES';
			    select.appendChild(option);
				header_text = document.createElement('label');
				header_text.innerHTML = 'Substitue ';
				header_text.htmlFor = select.id;
				document.getElementById('extra_log_event_row_1').insertCell(4).appendChild(header_text).appendChild(select);				
				
				select = document.createElement('select');
				select.id = 'select_batsman';
				inn.battingCard.forEach(function(bc,index,arr){
					if(bc.status.toLowerCase() == 'not out') {
						option = document.createElement('option');
						option.value = bc.player.playerId;
					    option.text = bc.player.ticker_name;
					    if(bc.onStrike.toLowerCase() == 'yes') {
						    option.selected = true;
					    }
					    select.appendChild(option);
					}
				});
				header_text = document.createElement('label');
				header_text.innerHTML = 'Batsman ';
				header_text.htmlFor = select.id;
				document.getElementById('extra_log_event_row_1').insertCell(5).appendChild(header_text).appendChild(select);
				
			}
		});

		select = document.createElement('select');
		select.id = 'select_batsman_runs';
		for(var i=0;i<=8;i++){
			option = document.createElement('option');
			option.value = i;
		    option.text = option.value;
		    select.appendChild(option);
		}
		header_text = document.createElement('label');
		header_text.innerHTML = 'Batsman runs ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(6).appendChild(header_text).appendChild(select);

		div = document.createElement('div');
		
	    option = document.createElement('input');
	    option.type = 'button';
	    option.name = 'log_wicket_btn';
	    option.id = option.name;
	    option.value = 'Log Wicket';
	    option.setAttribute('onclick','processCricketProcedures("LOG_WICKET",this);');
	    
	    div.append(option);
	    
		option = document.createElement('input');
		option.type = 'button';
		option.name = 'cancel_wicket_btn';

		option.id = option.name;
		option.value = 'Cancel';
		option.style.marginTop = '8px'; 
		option.setAttribute('onclick','processUserSelection(this)');

	    div.append(document.createElement('br'));
	    div.append(option);

	    document.getElementById('extra_log_event_row_1').insertCell(7).appendChild(div);

		break;

	case 'LOAD_ANY_BALL':

		select = document.createElement('select');
		select.id = 'select_delivery_type';
		for(var i=0;i<=2;i++) {
			option = document.createElement('option');
			switch (i) {
			case 0:
			    option.value = '';
				break;
			case 1:
			    option.value = 'wide';
				break;
			case 2:
			    option.value = 'no_ball';
				break;
			}
		    option.text = option.value.replace(/(^\w{1})|(\s{1}\w{1})/g, match => match.toUpperCase()).replace('_',' ');
		    select.appendChild(option);
		}

		header_text = document.createElement('label');
		header_text.innerHTML = 'Delivery type ';
		header_text.htmlFor = select.id;
	    
		$('#extra_log_event_row_1').empty();
		//$('#extra_log_event_row_2').empty();
		document.getElementById('extra_log_event_row_1').insertCell(0).appendChild(header_text).appendChild(select);
		//$('#' + select.id).select2({dropdownAutoWidth : true});
		
		select = document.createElement('select');
		select.id = 'select_how_out';
			
		for(var i=0;i<=16;i++) {
			option = document.createElement('option');
			switch (i) {
			case 0:
			    option.value = '';
				break;
			case 1:
			    option.value = 'caught';
				break;
			case 2:
			    option.value = 'caught_and_bowled';
				break;
			case 3:
			    option.value = 'bowled';
				break;
			case 4:
			    option.value = 'lbw';
				break;
			case 5:
			    option.value = 'stumped';
				break;
			case 6:
			    option.value = 'run_out';
				break;
			case 7:
			    option.value = 'hit_wicket';
				break;
			case 8:
			    option.value = 'handled_the_ball';
				break;
			case 9:
			    option.value = 'hit_ball_twice';
				break;
			case 10:
			    option.value = 'obstructing_fielder';
				break;
			case 11:
			    option.value = 'timed_out';
				break;
			case 12:
			    option.value = 'retired_hurt';
				break;
			case 13:
			    option.value = 'mankad';
				break;
			case 14:
			    option.value = 'absent_hurt';
				break;
			case 15:
			    option.value = 'concussed';
				break;
			case 16:
			    option.value = 'retired_out';
				break;
			}
		    option.text = option.value.replace(/(^\w{1})|(\s{1}\w{1})/g, match => match.toUpperCase()).replace('_',' ');
		    select.appendChild(option);
		}
		
		header_text = document.createElement('label');
		header_text.innerHTML = 'How out ';
		header_text.htmlFor = select.id;
	    
		document.getElementById('extra_log_event_row_1').insertCell(1).appendChild(header_text).appendChild(select);
		//$('#' + select.id).select2({dropdownAutoWidth : true});

		select = document.createElement('select');
		select.id = 'select_concussion_replacement_player';
		option = document.createElement('option');
		option.value = '';
	    option.text = '';
	    select.appendChild(option);
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
				if(inn.battingTeamId == match_data.setup.homeTeamId && match_data.setup.homeOtherSquad != null) {
					match_data.setup.homeOtherSquad.forEach(function(hos,index,arr){
						option = document.createElement('option');
						option.value = hos.playerId;
					    option.text = hos.ticker_name;
					    select.appendChild(option);
					});
				} else if(inn.battingTeamId == match_data.setup.awayTeamId && match_data.setup.awayOtherSquad != null) {
					match_data.setup.awayOtherSquad.forEach(function(hos,index,arr){
						option = document.createElement('option');
						option.value = hos.playerId;
					    option.text = hos.ticker_name;
					    select.appendChild(option);
					});
				}
			}
		});
		header_text = document.createElement('label');
		header_text.innerHTML = 'Replacement batsman ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(2).appendChild(header_text).appendChild(select);
		//$('#' + select.id).select2({dropdownAutoWidth : true});
		document.getElementById('extra_log_event_row_1').cells[2].style.display = 'none';
		
		match_data.match.inning.forEach(function(inn,index,arr){
			if(inn.isCurrentInning.toLowerCase() == 'yes') {
				
				select = document.createElement('select');
				select.id = 'select_out_batsman';
				inn.battingCard.forEach(function(bc,index,arr){
					if(bc.status.toLowerCase() == 'not out') {
						option = document.createElement('option');
						option.value = bc.player.playerId;
					    option.text = bc.player.ticker_name;
					    if(bc.onStrike.toLowerCase() == 'yes') {
						    option.selected = true;
					    }
					    select.appendChild(option);
					}
				});
				header_text = document.createElement('label');
				header_text.innerHTML = 'Out batsman ';
				header_text.htmlFor = select.id;
				document.getElementById('extra_log_event_row_1').insertCell(3).appendChild(header_text).appendChild(select);

				select = document.createElement('select');
				select.id = 'select_out_fielder';
				switch(index) {
				case 0: case 1:
					which_inn = 1 - index;
					break;
				case 2: 
					if(match_data.match.inning[1].battingTeamId == match_data.match.inning[2].battingTeamId) {
						which_inn = 0;
					} else {
						which_inn = index - 1;
					}
					break;
				case 3:
					which_inn = index - 1;
					break;
				}
				match_data.match.inning[which_inn].battingCard.forEach(function(bc,bc_index,bc_arr){
					option = document.createElement('option');
					option.value = bc.player.playerId;
				    option.text = bc.player.ticker_name;
					if(bc.player.captainWicketKeeper.toLowerCase().includes('wicket_keeper')) {
						option.selected = true;
					}
				    select.appendChild(option);
				});
				if(match_data.match.inning[which_inn].battingTeamId == match_data.setup.homeTeamId) {
					match_data.setup.homeSquad.forEach(function(hs,bc_index,bc_arr){
						option = document.createElement('option');
						option.value = hs.playerId;
					    option.text = hs.ticker_name;
					    select.appendChild(option);
					});
					if(match_data.setup.homeSubstitutes != null) {
						match_data.setup.homeSubstitutes.forEach(function(hs,bc_index,bc_arr){
							option = document.createElement('option');
							option.value = hs.playerId;
						    option.text = hs.ticker_name;
						    select.appendChild(option);
						});
					}
					if(match_data.setup.homeOtherSquad != null) {
						match_data.setup.homeOtherSquad.forEach(function(hs,bc_index,bc_arr){
							option = document.createElement('option');
							option.value = hs.playerId;
						    option.text = hs.ticker_name;
						    select.appendChild(option);
						});
					}
				}else if(match_data.match.inning[which_inn].battingTeamId == match_data.setup.awayTeamId) {
					match_data.setup.awaySquad.forEach(function(as,bc_index,bc_arr){
						option = document.createElement('option');
						option.value = as.playerId;
					    option.text = as.ticker_name;
					    select.appendChild(option);
					});
					if(match_data.setup.awaySubstitutes != null) {
						match_data.setup.awaySubstitutes.forEach(function(as,bc_index,bc_arr){
							option = document.createElement('option');
							option.value = as.playerId;
						    option.text = as.ticker_name;
						    select.appendChild(option);
						});
					}
					if(match_data.setup.awayOtherSquad != null) {
						match_data.setup.awayOtherSquad.forEach(function(as,bc_index,bc_arr){
							option = document.createElement('option');
							option.value = as.playerId;
						    option.text = as.ticker_name;
						    select.appendChild(option);
						});
					}
				}
				//Don't know fielder (substitute)
				option = document.createElement('option');
				option.value = -1;
			    option.text = "Substitute (Don't Know)";
			    select.appendChild(option);
				
				header_text = document.createElement('label');
				header_text.innerHTML = 'Fielder ';
				header_text.htmlFor = select.id;
				document.getElementById('extra_log_event_row_1').insertCell(4).appendChild(header_text).appendChild(select);
				removeDuplicateOptions(select.id);

				$('#' + select.id + ' > option').each(function() {
					if(match_data.match.inning[which_inn].battingTeamId == match_data.setup.homeTeamId) {
						for(var plyr = 0; plyr <= match_data.setup.homeSquad.length-1; plyr++) {
							if(match_data.setup.homeSquad[plyr].captainWicketKeeper.toLowerCase().includes('wicket_keeper')
								&& match_data.setup.homeSquad[plyr].playerId == this.value) {
								this.selected = true;
								return false;
							}
						}
					}else if(match_data.match.inning[which_inn].battingTeamId == match_data.setup.awayTeamId) {
						for(var plyr = 0; plyr <= match_data.setup.awaySquad.length-1; plyr++) {
							if(match_data.setup.awaySquad[plyr].captainWicketKeeper.toLowerCase().includes('wicket_keeper')
								&& match_data.setup.awaySquad[plyr].playerId == this.value) {
								this.selected = true;
								return false;
							}
						}
					}
				});				
				
				select = document.createElement('select');
				select.id = 'select_how_out_fielder_substitute';
				option = document.createElement('option');
				option.value = 'NO';
			    option.text = 'NO';
			    select.appendChild(option);
				option = document.createElement('option');
				option.value = 'YES';
			    option.text = 'YES';
			    select.appendChild(option);
				header_text = document.createElement('label');
				header_text.innerHTML = 'Substitute ';
				header_text.htmlFor = select.id;
				document.getElementById('extra_log_event_row_1').insertCell(5).appendChild(header_text).appendChild(select);				
				
				select = document.createElement('select');
				select.id = 'select_batsman';
				inn.battingCard.forEach(function(bc,index,arr){
					if(bc.status.toLowerCase() == 'not out') {
						option = document.createElement('option');
						option.value = bc.player.playerId;
					    option.text = bc.player.ticker_name;
					    if(bc.onStrike.toLowerCase() == 'yes') {
						    option.selected = true;
					    }
					    select.appendChild(option);
					}
				});
				header_text = document.createElement('label');
				header_text.innerHTML = 'Batsman scored ';
				header_text.htmlFor = select.id;
				document.getElementById('extra_log_event_row_1').insertCell(6).appendChild(header_text).appendChild(select);
				
				
			}
		});

		select = document.createElement('select');
		select.id = 'select_batsman_runs';
		for(var i=0;i<=10;i++){
			option = document.createElement('option');
			option.value = i;
		    option.text = option.value;
		    select.appendChild(option);
		}
		header_text = document.createElement('label');
		header_text.innerHTML = 'Batsman runs ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(7).appendChild(header_text).appendChild(select);
		
		select = document.createElement('select');
		select.id = 'select_runs_type';
		for(var i=0;i<=1;i++){
			option = document.createElement('option');
			if(i == 0) {
				option.value = 'boundary';
				if(match_data.setup.specialMatchRules != null && match_data.setup.specialMatchRules == 'ISPL'){
				    option.text = '4/6/9 is a boundary';
				} else {
				    option.text = '4/6 is a boundary';
				}
			} else {
				option.value = 'runs';
				if(match_data.setup.specialMatchRules != null && match_data.setup.specialMatchRules == 'ISPL'){
				    option.text = '4/6/9 are runs scored';
				} else {
				    option.text = '4/6 are runs scored';
				}
			}
		    select.appendChild(option);
		}
		header_text = document.createElement('label');
		header_text.innerHTML = 'Boundary ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(8).appendChild(header_text).appendChild(select);
		
		select = document.createElement('select');
		select.id = 'select_extra';
		for(var i=0;i<=5;i++){
			option = document.createElement('option');
			switch (i) {
			case 0:
				option.value = '';
				break;
			case 1:
				option.value = 'wide';
				break;
			case 2:
				option.value = 'no_ball';
				break;
			case 3:
				option.value = 'bye';
				break;
			case 4:
				option.value = 'leg_bye';
				break;
			case 5:
				option.value = 'penalty';
				break;
			}
		    option.text = option.value.replace(/(^\w{1})|(\s{1}\w{1})/g, match => match.toUpperCase()).replace('_',' ');
		    select.appendChild(option);
		}
		header_text = document.createElement('label');
		header_text.innerHTML = 'Extra ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(9).appendChild(header_text).appendChild(select);

		select = document.createElement('select');
		select.id = 'select_extra_runs';
		for(var i=0;i<=8;i++){
			option = document.createElement('option');
			option.value = i;
		    option.text = option.value;
		    select.appendChild(option);
		}
	    select.setAttribute('onchange','processUserSelection(this);');
		
		header_text = document.createElement('label');
		header_text.innerHTML = 'Extra runs ';
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(10).appendChild(header_text).appendChild(select);

		select = document.createElement('input');
		select.type = 'checkbox';
		select.id = 'select_do_not_increment_ball';
		header_text = document.createElement('label');
		header_text.innerHTML = "Don't increment ball ";
		header_text.htmlFor = select.id;
		document.getElementById('extra_log_event_row_1').insertCell(11).appendChild(header_text).appendChild(select);

		div = document.createElement('div');

	    option = document.createElement('input');
	    option.type = 'button';
	    option.name = 'log_any_ball_btn';
	    option.id = option.name;
	    option.value = 'Log Any Ball';
	    option.setAttribute('onclick','processCricketProcedures("LOG_ANY_BALL",this);');
	    
	    div.append(option);
	    
		option = document.createElement('input');
		option.type = 'button';
		option.name = 'cancel_any_ball_btn';

		option.id = option.name;
		option.value = 'Cancel';
		option.style.marginTop = '8px'; 
		option.setAttribute('onclick','processUserSelection(this)');
	    div.append(option);

	    document.getElementById('extra_log_event_row_1').insertCell(12).appendChild(div);
		
		$('html,body').animate({scrollTop: document.body.scrollHeight},"fast");

		break;
		
	case 'LOAD_EVENTS':
		
		$('#select_event_div').empty();
		
		table = document.createElement('table');
		table.setAttribute('class', 'table table-bordered events-table');
				
		tbody = document.createElement('tbody');
		
		for(var iRow=0;iRow<=1;iRow++) {
			
			row = tbody.insertRow(tbody.rows.length);
			row.setAttribute('id', 'load_events_row_' + iRow);
			if(iRow == 0) {
				max_cols = 11;
			} else {
				if(match_data.setup.specialMatchRules != null && match_data.setup.specialMatchRules == 'ISPL'){
					max_cols = 11;
				} else {
					max_cols = 10;
				}
			}
			
			for(var iCol=0;iCol<=max_cols;iCol++) {
				
				cell = row.insertCell(iCol);
				
				option = document.createElement('input');
				option.type = 'button';
				option.name = 'log_event_btn';
				
				switch (iRow) {
				case 0:
					
					switch (iCol) {
					case 0:
						option.id = '0';
						option.value = '0';
						break;
					case 1:
						option.id = '1';
						option.value = '1';
						break;
					case 2:
						option.id = '2';
						option.value = '2';
						break;
					case 3:
						option.id = '3';
						option.value = '3';
						break;
					case 4:
						option.id = '4';
						option.value = '4';
						break;
					case 5:
						option.id = '5';
						option.value = '5';
						break;
					case 6:
						option.id = '6';
						option.value = '6';
						break;
					case 7:
						option.id = 'wide';
						option.value = 'Wide';
						break;
					case 8:
						option.id = 'no_ball';
						option.value = 'No Ball';
						if(parseInt(match_data.setup.noBallsRuns) > 1) {
							option.value = option.value + ' (' + match_data.setup.noBallsRuns + ')';
						}
						break;
					case 9:
						option.id = 'wicket';
						option.value = 'Wicket';
						break;
					case 10:
						option.id = 'any_ball';
						option.value = 'Any Ball';
						break;
					case 11:
						if(match_data.setup.matchType.toUpperCase() == 'TEST' || match_data.setup.matchType.toUpperCase() == 'FC') {
							option.id = 'impact';
							option.value = 'Impact/Concussed';
						} else {
							option.id = 'PP';
							option.value = 'PP';
						}
						break;
					}
					break;
					
				case 1:
					
					switch (iCol) {
					case 0:
						option.id = 'swap_batsman';
						option.value = 'Swap';
						break;
					case 1:
						option.id = 'change_bowler';
						option.value = 'Bowler';
						break;
					case 2:
						option.id = 'new_batsman';
						option.value = 'Batsman';
						break;
					case 3:
						option.id = 'bye';
						option.value = 'Bye';
						break;
					case 4:
						option.id = 'leg_bye';
						option.value = 'Leg Bye';
						break;
					case 5:
						option.id = 'undo';
						option.value = 'Undo';
						break;
					case 6:
						option.id = 'overwrite';
						option.value = 'Overwrite';
						break;
					case 7:
						option.id = 'review';
						option.value = 'Review';
						break;
					case 8:
						option.id = 'end_over';
						option.value = 'End Over';
						break;
					case 9:
						option.id = 'result';
						option.value = 'Result';
						break;
					case 10:
						if(match_data.setup.matchType.toUpperCase() == 'TEST' || match_data.setup.matchType.toUpperCase() == 'FC') {
							option.id = 'finish';
							option.value = 'Finish Time';
						} else {
							option.id = 'impact';
							option.value = 'Impact/Concussed';
						}
						break;
					case 11:
						if(match_data.setup.specialMatchRules != null && match_data.setup.specialMatchRules == 'ISPL'){
							option.id = '9';
							option.value = '9';
						}
						break;
					/*case 11:
						if(match_data.setup.specialMatchRules != null && match_data.setup.specialMatchRules == 'ISPL'){
							option.id = '50-50';
							option.value = '50-50';
						}  
						break;*/
					}
				}
				
				if(option.id) {
					
					switch (option.id) {
					case 'bye': case 'leg_bye': case '4': case '6': case '9': case 'overwrite': case 'various': case 'new_batsman': case 'wide':
					 
						option.className = 'btn btn-sm btn-cricket dropdown-toggle';
						option.setAttribute('aria-haspopup', 'true');
						option.setAttribute('aria-expanded', 'false');
						option.title = option.value;

						var newBtn = document.createElement('button');
						newBtn.type = 'button';
						newBtn.id = option.id;
						newBtn.className = option.className;
						newBtn.innerText = option.value;
						newBtn.title = option.title;
						option = newBtn;
						
						option.setAttribute('data-bs-toggle', 'dropdown');
						
						/*option.setAttribute('data-toggle', 'dropdown');
						option.setAttribute('aria-haspopup', 'true');
						option.setAttribute('aria-expanded', 'false');					
						option.className = 'btn btn-secondary btn-sm'; */
						
						div = document.createElement('div');
					    div.id = option.id + '_div';
					    div.append(option);
					    div.className = 'dropdown d-inline-block';
					    //div.setAttribute('onclick','processUserSelection(this)');
					    
					    linkDiv = document.createElement('div');
					    linkDiv.className = 'dropdown-menu';
					    linkDiv.setAttribute('aria-labelledby',option.id);

						switch (option.id) {
						case 'overwrite':
						
							for(var i=1; i<=7; i++) {
						    	anchor = document.createElement('a');
						    	anchor.className = 'dropdown-item cricket-item';
							    switch (i) {
								case 1:
								    anchor.id=option.id + '_teams_total';
								    anchor.innerText=option.id.replace(/(^\w{1})|(\s{1}\w{1})/g, match => match.toUpperCase()).replace('_',' ') + ' Team Total';
									break;
								case 2:
								    anchor.id=option.id + '_teams_extras';
								    anchor.innerText=option.id.replace(/(^\w{1})|(\s{1}\w{1})/g, match => match.toUpperCase()).replace('_',' ') + ' Team Extras';
									break;
								case 3:
								    anchor.id=option.id + '_batsman_stats';
								    anchor.innerText=option.id.replace(/(^\w{1})|(\s{1}\w{1})/g, match => match.toUpperCase()).replace('_',' ') + ' Batsman Stats';
									break;
								case 4:
								    anchor.id=option.id + '_bowler_figures';
								    anchor.innerText=option.id.replace(/(^\w{1})|(\s{1}\w{1})/g, match => match.toUpperCase()).replace('_',' ') + ' Bowler Figures';
									break;
								case 5:
								    anchor.id=option.id + '_batsman_howout';
								    anchor.innerText=option.id.replace(/(^\w{1})|(\s{1}\w{1})/g, match => match.toUpperCase()).replace('_',' ') + ' Batsman Howout';
									break;
								case 6:
								    anchor.id=option.id + '_partnerships';
								    anchor.innerText=option.id.replace(/(^\w{1})|(\s{1}\w{1})/g, match => match.toUpperCase()).replace('_',' ') + ' Partnerships';
									break;
								case 7:
								    anchor.id=option.id + '_battingcard';
								    anchor.innerText=option.id.replace(/(^\w{1})|(\s{1}\w{1})/g, match => match.toUpperCase()).replace('_',' ') + ' Batting Card';
									break;
								/*case 8:
								    anchor.id=option.id + '_substitution';
								    anchor.innerText=option.id.replace(/(^\w{1})|(\s{1}\w{1})/g, match => match.toUpperCase()).replace('_',' ') + ' Substitute';
									break;*/
								}
							    anchor.setAttribute('onclick','processUserSelection(this);');
							    anchor.style = 'display:block;';
							    linkDiv.append(anchor);
							}
							break;

						case '4': case '6': case '9':
						
							for(var ibound=1; ibound<=2; ibound++) {
						    	anchor = document.createElement('a');
						    	anchor.className = 'dropdown-item cricket-item';
							    if(ibound == 1) {
								    anchor.id=option.id + ',boundary';
								    anchor.innerText=option.id.replace(/(^\w{1})|(\s{1}\w{1})/g, match => match.toUpperCase()).replace('_',' ') + ' (Boundary)';
							    } else {
								    anchor.id=option.id + ',runs';
								    anchor.innerText=option.id.replace(/(^\w{1})|(\s{1}\w{1})/g, match => match.toUpperCase()).replace('_',' ') + ' (Runs Scored)';
							    }
							    anchor.setAttribute('onclick','processCricketProcedures("LOG_EVENT",this);');
							    //anchor.style = 'display:block;';
							    linkDiv.append(anchor);
							}
							
							break;

						case 'bye': case 'leg_bye': case 'wide':
							
							for(var ibye=1; ibye<=6; ibye++) {
						    	anchor = document.createElement('a');
						    	anchor.className = 'dropdown-item cricket-item';
							    anchor.id=option.id + ',' + ibye;
							    anchor.innerText=option.id.replace(/(^\w{1})|(\s{1}\w{1})/g, match => match.toUpperCase()).replace('_',' ') + ' ' + ibye;
							    anchor.setAttribute('onclick','processCricketProcedures("LOG_EVENT",this);');
							    anchor.style = 'display:block;';
							    linkDiv.append(anchor);
							}
							break;

						case 'new_batsman':

							var batters_plyrs = [];
							dataToProcess.match.inning.forEach(function(inn,index,arr) {
								if(inn.isCurrentInning.toLowerCase() == 'yes') {
									if(inn.battingTeamId == dataToProcess.setup.homeTeamId) {
										inn.battingCard.forEach(function(bc,index,arr){
											if((bc.status != null && bc.status.toLowerCase() == 'stilltobat') || (bc.howOut != null && bc.howOut.toLowerCase() == 'concussed')) {
												batters_plyrs.push(bc.player);
											}
										});
										if(match_data.setup.specialMatchRules != null && match_data.setup.specialMatchRules == 'ISPL'){
											match_data.setup.homeSquad.forEach(function(hs,bc_index,bc_arr){
												batters_plyrs.push(hs);
											});
										}
										if(match_data.setup.homeSubstitutes != null) {
											match_data.setup.homeSubstitutes.forEach(function(hs,bc_index,bc_arr){
												batters_plyrs.push(hs);
											});
										}
									}else if(inn.battingTeamId == dataToProcess.setup.awayTeamId) {
										inn.battingCard.forEach(function(bc,index,arr){
											if((bc.status != null && bc.status.toLowerCase() == 'stilltobat') || (bc.howOut != null && bc.howOut.toLowerCase() == 'concussed')) {
												batters_plyrs.push(bc.player);
											}
										});
										if(match_data.setup.specialMatchRules != null && match_data.setup.specialMatchRules == 'ISPL'){
											match_data.setup.awaySquad.forEach(function(as,bc_index,bc_arr){
												batters_plyrs.push(as);
											});
										}
										if(match_data.setup.awaySubstitutes != null) {
											match_data.setup.awaySubstitutes.forEach(function(as,bc_index,bc_arr){
												batters_plyrs.push(as);
											});
										}
									}
								}
							});
							
							batters_plyrs = batters_plyrs.filter((p, index, self) => index === self.findIndex(t => t.playerId === p.playerId));
								
							batters_plyrs.forEach(function(bt,index_cp,arr_cp) {
						    	anchor = document.createElement('a');
						    	anchor.className = 'dropdown-item cricket-item';
							    anchor.id = option.id + ',' + bt.playerId;
							    anchor.innerText= bt.ticker_name;
							    anchor.setAttribute('onclick','processCricketProcedures("LOG_EVENT",this);');
							    anchor.style = 'display:block;';
							    linkDiv.append(anchor);
							});
							break;
						}
					    div.append(linkDiv);
						cell.append(div);
						break;
						
					default:
						
						option.className = 'btn btn-sm btn-cricket';
						option.title = option.value;
						option.onclick = function() {processUserSelection(this)};
						cell.appendChild(option);
						
						break;
					
					}
				}
			}
		}

		row = tbody.insertRow(tbody.rows.length);
		row.id = 'extra_log_event_row_1';
		row.style.display = 'none';

		//row = tbody.insertRow(tbody.rows.length);
		//row.id = 'extra_log_event_row_2';
		//row.style.display = 'none';
					
		table.appendChild(tbody);
		document.getElementById('select_event_div').appendChild(table);

		break;
				
	case 'LOAD_MATCH': 
		
		$('#inning_div').empty();

		if (dataToProcess)
		{
			dataToProcess.match.inning.forEach(function(inns_item,index,arr){

				if(document.getElementById('select_match_innings').value == inns_item.inningNumber) {
			
					// Batting card
					header_text = document.createElement('header_text');
					header_text.innerHTML = inns_item.batting_team.teamName4 + ' Scorecard';
					header_text.style.fontSize = "12px";

					table = document.createElement('table');
					table.setAttribute('class', 'table table-bordered');
					table.style.fontSize = "12px";
					table.style.tableLayout = 'auto';    
					table.style.width = 'auto';          
					table.style.maxWidth = 'none';
					table.style.whiteSpace = 'nowrap';   
					
					tr = document.createElement('tr');
					for (var j = 0; j <= 4; j++) {
					    th = document.createElement('th'); //column
					    switch (j) {
						case 0:
						    text = document.createTextNode('Batsmen');
							break;
						case 1:
						    text = document.createTextNode('How Out'); 
							break;
						case 2:
						    text = document.createTextNode('Runs'); 
							break;
						case 3:
						    text = document.createTextNode('Balls'); 
							break;
						case 4:
							if(match_data.setup.specialMatchRules != null && match_data.setup.specialMatchRules == 'ISPL'){
							    text = document.createTextNode('4s/6s/9s');
							} else {
							    text = document.createTextNode('4s/6s');
							}
							break;
						}
					    th.appendChild(text);
					    tr.appendChild(th);
					}
				    
					thead = document.createElement('thead');
					thead.appendChild(tr);
					table.appendChild(thead);
					
					tbody = document.createElement('tbody');
					
					inns_item.battingCard.sort((a,b) => (a.batterPosition > b.batterPosition) ? 1 : ((b.batterPosition > a.batterPosition) ? -1 : 0));
					
					inns_item.battingCard.forEach(function(bat_tm_item,index,arr){
						
						 if((bat_tm_item.status != null && bat_tm_item.status.toLowerCase() != 'stilltobat') 
								|| (bat_tm_item.howOut != null && bat_tm_item.howOut.trim() != '')) {
									
							  row = tbody.insertRow(tbody.rows.length);
								
							  cell = row.insertCell(0);
							  cell.innerHTML = bat_tm_item.batterPosition + '. ' + bat_tm_item.player.ticker_name;
							  cell.title = bat_tm_item.player.full_name;
							  
							  cell = row.insertCell(1);
							  if(bat_tm_item.status.replace('_', ' ').toLowerCase() == 'not out') {
								  cell.innerHTML = 'Not Out';
							  } else if(bat_tm_item.status.toLowerCase() == 'still_to_bat') {
							    cell.innerHTML = 'Did Not Bat';
							  } else if(bat_tm_item.howOut.toLowerCase() == 'retired_hurt') {
								  cell.innerHTML = 'retired hurt';
							  } else if(bat_tm_item.howOut.toLowerCase() == 'absent_hurt') {
								  cell.innerHTML = 'absent hurt';
							  } else {
								  cell.innerHTML = bat_tm_item.howOutText;
							  }

							  cell = row.insertCell(2);
							  cell.innerHTML = bat_tm_item.runs;
							  cell = row.insertCell(3);
							  cell.innerHTML = bat_tm_item.balls + ' (' 
							  	+ secondsTimeSpanToMinutesAndSeconds(bat_tm_item.duration) + ')';
							  cell = row.insertCell(4);
							  if(match_data.setup.specialMatchRules != null && match_data.setup.specialMatchRules == 'ISPL'){
								  cell.innerHTML = bat_tm_item.fours + '/' + bat_tm_item.sixes + '/' + bat_tm_item.nines;
							  } else {
								  cell.innerHTML = bat_tm_item.fours + '/' + bat_tm_item.sixes;
							  }
							  
							  if(bat_tm_item.onStrike != null && bat_tm_item.onStrike.toLowerCase() == 'yes') {
								$(row).find('td').css({color: 'white','background-color': 'brown'});
							  } 
						  }

					});

				    row = tbody.insertRow(tbody.rows.length);
					cell = row.insertCell(0);
					
					text = '';
					if(inns_item.totalWides > 0) {
						if(text) {
							text = text + ', Wd:' + inns_item.totalWides;
						} else {
							text = 'Wd:' + inns_item.totalWides;
						}
					}
					if(inns_item.totalNoBalls > 0) {
						if(text) {
							text = text + ', Nb:' + inns_item.totalNoBalls;
						} else {
							text = 'Nb:' + inns_item.totalNoBalls;
						}
					}
					if(inns_item.totalByes > 0) {
						if(text) {
							text = text + ', B:' + inns_item.totalByes;
						} else {
							text = 'B:' + inns_item.totalByes;
						}
					}
					if(inns_item.totalLegByes > 0) {
						if(text) {
							text = text + ', Lb:' + inns_item.totalLegByes;
						} else {
							text = 'Lb:' + inns_item.totalLegByes;
						}
					}
					if(inns_item.totalPenalties > 0) {
						if(text) {
							text = text + ', P:' + inns_item.totalPenalties;
						} else {
							text = 'P:' + inns_item.totalPenalties;
						}
					}
					cell.innerHTML = 'Extras: ' + inns_item.totalExtras; 
					if(text) {
						cell.innerHTML = cell.innerHTML + ' (' + text + ')'; 
					}

					cell = row.insertCell(1);
					cell.innerHTML = 'Total: ' + inns_item.totalRuns + '-' + inns_item.totalWickets;
					if(dataToProcess.setup.specialMatchRules != null && dataToProcess.setup.specialMatchRules == 'ISPL'){
						
						if(match_data.eventFile.events != null) {
							for (var i = match_data.eventFile.events.length - 1; i >= 0; i--) {

							  if (match_data.eventFile.events[i].eventExtra != null && match_data.eventFile.events[i].eventExtra.toUpperCase() == 'CHALLENGE' 
							  	&& match_data.eventFile.events[i].eventInningNumber == inns_item.inningNumber
							  	&& inns_item.totalOvers >= parseInt(match_data.eventFile.events[i].eventOverNo + 1)) {

								if(inns_item.specialRuns) {
									if (inns_item.specialRuns.startsWith('+')) {
										cell.innerHTML = 'Total: ' + parseInt(inns_item.totalRuns + parseInt(inns_item.specialRuns.replace('+', ''))) + '-' + inns_item.totalWickets;
									}else if (inns_item.specialRuns.startsWith('-')) {
										cell.innerHTML = 'Total: ' + parseInt(inns_item.totalRuns - parseInt(inns_item.specialRuns.replace('-', ''))) + '-' + inns_item.totalWickets;
									}
								}
								break;
							  }
							}
						}
					}

					if(inns_item.isDeclared != null && inns_item.isDeclared.toUpperCase() == 'YES') {
						cell.innerHTML = cell.innerHTML + 'd';
					} 
					cell.innerHTML = cell.innerHTML + ' (' + inns_item.totalOvers + '.' + inns_item.totalBalls + ')';
					if(dataToProcess.setup.targetOvers != null && dataToProcess.setup.targetOvers.trim() !== "") {
						cell.innerHTML = cell.innerHTML + ' [' + dataToProcess.setup.targetOvers + ']';
					} else if(dataToProcess.setup.reducedOvers > 0) {
						cell.innerHTML = cell.innerHTML + ' [' + dataToProcess.setup.reducedOvers + ']';
					}
					if(dataToProcess.setup.specialMatchRules != null && dataToProcess.setup.specialMatchRules == 'ISPL'){
						if(inns_item.specialRuns != null && dataToProcess.eventFile != null && dataToProcess.eventFile.events.length > 0 
							&& dataToProcess.eventFile.events.some(e => e.eventType.toUpperCase() === 'CHANGE_BOWLER' 
							&& e.eventExtra.toUpperCase().includes('CHALLENGE') && e.eventInningNumber == inns_item.inningNumber)) 
					    {
							cell.innerHTML = cell.innerHTML + ' {CHLNG: ' + inns_item.specialRuns + '}';
						}
					}
					
					table.appendChild(tbody);

				    div = document.createElement('div');
				    div.className = 'row';
					div.style.display = 'flex';
					div.style.flexWrap = 'nowrap';
					div.style.alignItems = 'flex-start';
					div.style.width = '100%';

				    linkDiv = document.createElement('div');
				    linkDiv.className = 'col-md-6 card-col';
					linkDiv.style.flex = '0 0 auto';
					linkDiv.style.minWidth = 'max-content';   
					linkDiv.style.boxSizing = 'border-box';  
					linkDiv.style.overflow = 'visible';  
				    
					linkDiv.appendChild(header_text);				
					linkDiv.appendChild(table);
					div.appendChild(linkDiv);
									
					// Bowling card
					header_text = document.createElement('header_text');
					header_text.innerHTML = inns_item.bowling_team.teamName4 + ' Bowling Card';
					header_text.style.fontSize = "12px";

					table = document.createElement('table');
					table.setAttribute('class', 'table table-bordered');
					table.style.fontSize = "12px";
					table.style.tableLayout = 'auto';    
					table.style.width = 'auto';          
					table.style.maxWidth = 'none';
					table.style.whiteSpace = 'nowrap';   

					tr = document.createElement('tr');
					for (var j = 0; j <= 4; j++) {
					    th = document.createElement('th'); //column
					    switch (j) {
						case 0:
						    text = document.createTextNode('Bowler'); 
							break;
						case 1:
						    text = document.createTextNode('Ovrs'); 
							break;
						case 2:
						    text = document.createTextNode('Runs'); 
							break;
						case 3:
						    text = document.createTextNode('Wkts'); 
							break;
						case 4:
						    text = document.createTextNode('Dots/Mdns'); 
							break;
						}
					    th.appendChild(text);
					    tr.appendChild(th);
					}
				    
					thead = document.createElement('thead');
					thead.appendChild(tr);
					table.appendChild(thead);
					
					tbody = document.createElement('tbody');
					
					if(inns_item.bowlingCard != null) {
						inns_item.bowlingCard.forEach(function(bwl_tm_item,index,arr){
							  
							  row = tbody.insertRow(tbody.rows.length);
								
							  cell = row.insertCell(0);
							  cell.innerHTML = bwl_tm_item.player.ticker_name;
							  cell.title = bwl_tm_item.player.full_name;

							  cell = row.insertCell(1);
							  cell.innerHTML = bwl_tm_item.overs + '.' + bwl_tm_item.balls;
							  
							  cell = row.insertCell(2);
							  cell.innerHTML = bwl_tm_item.runs;

							  cell = row.insertCell(3);
							  cell.innerHTML = bwl_tm_item.wickets;

							  cell = row.insertCell(4);
							  cell.innerHTML = bwl_tm_item.dots + '/' + bwl_tm_item.maidens;

						      if(bwl_tm_item.status != null && bwl_tm_item.status.toLowerCase() == 'currentbowler') {
								  $(row).find('td').css({color: 'white','background-color': 'brown'});
						      }

						});
					}

					table.appendChild(tbody);

				    linkDiv = document.createElement('div');
				    linkDiv.className = 'col-md-6 card-col';
					linkDiv.style.flex = '0 0 auto';
					linkDiv.style.minWidth = 'max-content';     
					linkDiv.style.boxSizing = 'border-box';
					linkDiv.style.overflow = 'visible';  
				    
					linkDiv.appendChild(header_text);				
					linkDiv.appendChild(table);
					div.appendChild(linkDiv);

					document.getElementById('inning_div').appendChild(div);
					
				}
				
			});

			table = document.createElement('table');
			table.setAttribute('class', 'table table-bordered');
			tbody = document.createElement('tbody');

			table.appendChild(tbody);
			document.getElementById('inning_div').appendChild(table);

			row = tbody.insertRow(tbody.rows.length);

			header_text = document.createElement('h6');
			header_text.id = 'match_time_hdr';
			header_text.style.fontSize = "12px";
			row.insertCell(0).appendChild(header_text);

			header_text = document.createElement('h6');
			header_text.id = 'toss_result_hdr';
			if(dataToProcess.setup.tossWinningTeam == dataToProcess.setup.homeTeamId) {
				header_text.innerHTML = 'TOSS: ' + dataToProcess.setup.homeTeam.teamName4;
			} else if(dataToProcess.setup.tossWinningTeam == dataToProcess.setup.awayTeamId) {
				header_text.innerHTML = 'TOSS: ' + dataToProcess.setup.awayTeam.teamName4;
			}
			header_text.style.fontSize = "12px";
			row.insertCell(1).appendChild(header_text);

			header_text = document.createElement('h6');
			header_text.id = 'match_status_hdr';
			header_text.innerHTML = 'Match Status: ' + dataToProcess.match.matchStatus;
			header_text.style.fontSize = "12px";
			row.insertCell(2).appendChild(header_text);

			if(dataToProcess.eventFile != null) {
				if(dataToProcess.eventFile.events != null && dataToProcess.eventFile.events.length > 0) {
					max_cols = dataToProcess.eventFile.events.length;
					if (max_cols > 20) {
						max_cols = 20;
					}
					header_text = document.createElement('h6');
					for(var i = 0; i < max_cols; i++) {
						if(dataToProcess.eventFile.events[(dataToProcess.eventFile.events.length - 1) - i].eventType.toUpperCase() != 'SHOT'
							&& dataToProcess.eventFile.events[(dataToProcess.eventFile.events.length - 1) - i].eventType.toUpperCase() != 'WAGON') {
							if(dataToProcess.eventFile.events[(dataToProcess.eventFile.events.length - 1) - i].eventDescription) {
								text = dataToProcess.eventFile.events[(dataToProcess.eventFile.events.length - 1) - i].eventDescription.replaceAll('_',' ').replaceAll(',','|');
							} else {
								text = dataToProcess.eventFile.events[(dataToProcess.eventFile.events.length - 1) - i].eventType.replaceAll('_',' ').replaceAll(',','|');
							}
							if(parseInt(dataToProcess.setup.noBallsRuns) > 1) {
								text = text.replaceAll('NO BALL',dataToProcess.setup.noBallsRuns + 'NB')
							}
							if(header_text.innerHTML) {
								header_text.innerHTML = header_text.innerHTML + ',' + text; 
							} else {
								header_text.innerHTML = text; 
							}
						}
					}
					header_text.innerHTML = 'Events: ' + header_text.innerHTML;
					header_text.style.fontSize = "12px";
					document.getElementById('inning_div').appendChild(header_text);
				}
			}
		}
		break;
	}
}
function MatchTimeSecondsToHhMmSs(d) {
    return 'Match Time ' + ('0' + Math.floor(d / 3600)).slice(-2) + ':' + 
    	('0' + Math.floor(d % 3600 / 60)).slice(-2) + ':' + ('0' + Math.floor(d % 3600 % 60)).slice(-2); 
}
/*function checkEmpty(inputBox,textToShow) {

	var name = $(inputBox).attr('id');
	
	document.getElementById(name + '-validation').innerHTML = '';
	document.getElementById(name + '-validation').style.display = 'none';
	$(inputBox).css('border','');
	if(document.getElementById(name).value.trim() == '') {
		$(inputBox).css('border','#E11E26 2px solid');
		document.getElementById(name + '-validation').innerHTML = textToShow + ' required';
		document.getElementById(name + '-validation').style.display = '';
		document.getElementById(name).focus({preventScroll:false});
		return false;
	}
	return true;	
}*/
function getFullOrdinalText(ordinal_number)
{
	switch (ordinal_number) {
	case 1:
		return 'First';
	case 2:
		return 'Second';
	case 3:
		return 'Third';
	default:
		return ordinal_number;
	}
}
function getFullEventTypeWord(event_type)
{
	switch (event_type) {
	case '0':
		return 'Dot';
	case '1':
		return 'Single';
	case '2':
		return 'Two';
	case '3':
		return 'Three';
	case '4':
		return 'Four';
	case '5':
		return 'Five';
	case '6':
		return 'Six';
	default:
		return event_type.replace(/(^\w{1})|(\s{1}\w{1})/g, match => match.toUpperCase()).replace('_',' ');
	}
}
function removeSelectDuplicates(selectAttrToUse,selectNameId)
{
	var this_list = {};
	$("select[" + selectAttrToUse + "='" + selectNameId + "'] > option").each(function () {
	    if(this_list[this.text]) {
	        $(this).remove();
	    } else {
	        this_list[this.text] = this.value;
	    }
	});
}
function findSelectDuplicates(selectAttrToUse,selectNameId)
{
	var duplicatesTxt = [], selectedValue = '';
	$("select[" + selectAttrToUse + "='" + selectNameId + "'] > option:selected").each(function () {
	   selectedValue = $(this).val();
	   $("select[" + selectAttrToUse + "='" + selectNameId + "'] > option:selected").not(this).each(function () {
	       if ($(this).val() == selectedValue) {
			   duplicatesTxt.push($(this).text()); 
		   }
	   });	
	});	
	return duplicatesTxt.filter(function(elem, index, self) {return index === self.indexOf(elem);}).toString();
}
function removeDuplicateOptions(selectId) {
  const select = document.getElementById(selectId);
  const seenValues = new Set();

  for (let i = select.options.length - 1; i >= 0; i--) {
    const option = select.options[i];
    const key = option.value; 
    if (seenValues.has(key)) {
      select.remove(i);
    } else {
      seenValues.add(key);
    }
  }
}
function setEventsLayoutSingleColumn(isSingle) {
  var table = document.querySelector('#select_event_div table.events-table');
  if (!table) return;

  // remove both modifier classes first
  table.classList.remove('single-column-events', 'two-column-events');

  if (isSingle) {
    // force single column
    table.classList.add('single-column-events');
  } else {
    // default to TWO columns when not single
    table.classList.add('two-column-events');
  }
}
function normalizeAndAppendTable(table, wrapperDiv, anchorRowId) 
{
  if (!table || !wrapperDiv) return;

  Object.assign(table.style, { display: 'table', width: '100%', tableLayout: 'fixed' });

  (thead => { if (thead) thead.style.display = 'table-header-group'; })(table.querySelector('thead'));
  (tbody => { if (tbody) tbody.style.display = 'table-row-group'; })(table.querySelector('tbody'));

  table.querySelectorAll('tr').forEach(r => {
    r.style.display = 'table-row';
    r.querySelectorAll('th,td').forEach(c => {
      c.style.display = 'table-cell';
      c.style.whiteSpace = 'normal';        
      c.style.wordBreak = 'break-word';     
      c.style.overflowWrap = 'anywhere';    
    });
  });

  var anchor = document.getElementById(anchorRowId);
  if (anchor) {
    const tag = anchor.tagName ? anchor.tagName.toLowerCase() : '';

    if (tag === 'tr') {
      try {
        const newCell = anchor.insertCell
          ? anchor.insertCell(0) // works when anchor is a tr
          : null;
        if (newCell) {
          newCell.appendChild(wrapperDiv);
          Object.assign(newCell.style, { display: 'block', width: '100%', gridColumn: '1 / -1', whiteSpace: 'normal' });
          return;
        }
      } catch (e) {
      }
    }

    anchor.appendChild(wrapperDiv);
    return;
  }

  document.body.appendChild(wrapperDiv);
}
function GetFirstFewChars(str, count) {
    if (typeof str !== 'string' || !str.trim() || count <= 0) return null;
    return str.length >= count ? str.substring(0, count) : str;
}
$(document).ready(function(){
	var previous_selected_inning;
	$('#select_match_innings').on('focus', function () {
	   previous_selected_inning = this.value;
	}).on('change',function(){
		if($('#select_match_status option:selected').val() == 'start') {
			alert('Match status must be paused before you can change the innings');
			document.getElementById('select_match_innings').value = previous_selected_inning;
			return false;
		}
		processCricketProcedures('SELECT_INNING',$('#select_match_innings'));
	});
});
