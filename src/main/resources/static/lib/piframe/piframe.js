/***********************************************************
 *  PIFRAME by Alex Estela
 *  Licensed under MIT (https://opensource.org/licenses/MIT)
 **********************************************************/

var PIFRAME = {

	// params
	backendCtxRoot: "http://localhost:8080",
	getMediasURI: "/backend/api/v1/medias",
	getMediasRandomAttr: "?random=true",
	pingURI: "/backend/api/v1/tools/ping",
	loadingMsg: "CHARGEMENT EN COURS",
	errorMsg: "ERREUR DE CHARGEMENT",
	mediaDisplayTime: 8000,
	retryFrequency: 5000,
	retryTimeout: 600000,
	debugMode: false,
	trustMode: true,
	
	// tmp
	retryTimeSoFar: 0,
	mediaCount: 0,

	load: function() {
		$.ajax({
			type : "GET",
			url : PIFRAME.backendCtxRoot + PIFRAME.getMediasURI + PIFRAME.getMediasRandomAttr,
			dataType : "json",
			success : function(response) {
				
				if (!response || !response.length || response.length == 0) PIFRAME.reinit();
				else {
						
					if (PIFRAME.debugMode) console.log("Found " + response.length + " medias");
					
					for (var m in response) {
						var media = response[m];
						var id = media.id;
						var width = media.width;
						var height = media.height;
						var creationDate = media.created;
						var url = PIFRAME.backendCtxRoot + PIFRAME.getMediasURI + "/" + id;
						
						var finalHeight = $(window).height();
						if (height < finalHeight) finalHeight = height;
						var ratio = finalHeight / height;
						var finalWidth = width * ratio;
						if (finalWidth > width) {
							finalWidth = width;
							ratio = finalWidth / width;
							finalHeight = height * ratio;
						}
						
						var html = "<img class='mediaImg' width='" + Math.round(finalWidth) + "px' height='" + Math.round(finalHeight) + "px'/>";
						$(".mediaContainer").append(html);
						
						$(".mediaImg").last().attr("data-src", url);
						$(".mediaImg").last().attr("creation-date", creationDate); 
						if (PIFRAME.mediaCount == 0) PIFRAME.loadMedia(0);
			
						PIFRAME.mediaCount++;
						//if (PIFRAME.mediaCount > 3) break;
					}
					
					$(".loadingContainer").hide();				
					var owl = $(".mediaContainer");
					owl.owlCarousel({
						navigation: false,
						singleItem: true,
						autoPlay: PIFRAME.mediaDisplayTime,
						lazyLoad: false,
						transitionStyle: "fade",
						pagination: false,
						center: true,
						afterAction: function(e) {
							
							var total = PIFRAME.mediaCount;
							var current = $(".mediaContainer").data("owlCarousel") ?
								$(".mediaContainer").data("owlCarousel").owl.currentItem : 0;
							if (PIFRAME.debugMode) console.log("Displaying " + (current+1) + "/" + total);
							
							$(".mediaImg").eq(current).css("visibility", "visible");

							// bugged dates coming from Smugmug
							//var creationDate = Date.parse($(".mediaImg").eq(current).attr("creation-date").substring(0, 10)).toString("d-MMM-yyyy");
							//console.log("creationDate = " + $(".mediaImg").eq(current).attr("creation-date").substring(0, 10));
							//$(".mediaTooltip").html(creationDate);
							
							//console.log($(".mediaImg").eq(current).attr("creation-date"));
							
							if (current > 0) {
								$(".mediaImg").eq(current-1).css("visibility", "hidden");
							}
							
							if (current+1 < total) {
								PIFRAME.loadMedia(current+1);
							}
							else {
								$(".mediaContainer").trigger('owl.stop');
								setTimeout(function() {
									PIFRAME.reinit();
								}, PIFRAME.mediaDisplayTime);
							}					
						}
					});
				}
			}
		});
	},

	loadMedia: function(index) {
		$(".mediaImg")
			.eq(index)
			.on('load', function() { 
				if (PIFRAME.debugMode) console.log("Loaded: " + index);
			})
			.on('error', function() { 
				if (!PIFRAME.trustMode) {
					if (PIFRAME.debugMode) console.log("Error while loading media, restarting from scratch");
					PIFRAME.reinit();
				}
			})
			.attr("src", $(".mediaImg").eq(index).attr("data-src"));		
	},
	
	reinit: function() {
		if ($(".mediaContainer").data("owlCarousel")) 
			$(".mediaContainer").data("owlCarousel").destroy();
		$(".mediaContainer").empty();
		PIFRAME.retryTimeSoFar = 0; 
		PIFRAME.mediaCount = 0; 
		if (PIFRAME.trustMode) PIFRAME.load();
		else PIFRAME.init();
	},
	
	init: function() {
		$(".mediaContainer").hide();
		$(".mediaTooltip").empty();
		$(".loadingContainer").html(PIFRAME.loadingMsg);
		$(".loadingContainer").show();
		PIFRAME.initDisplay();
	},

	initDisplay: function() {
		$.ajax({
			type: "GET",
			url: PIFRAME.backendCtxRoot + PIFRAME.pingURI,
			dataType: "json",
			success: function() {
				PIFRAME.load();
			},
			error: function() {
				PIFRAME.retryTimeSoFar += PIFRAME.retryFrequency;
				if (PIFRAME.retryTimeSoFar >= PIFRAME.retryTimeout) {
					$(".loadingContainer").html(PIFRAME.errorMsg);
					$(".loadingContainer").show();
				}
				else {
					if (PIFRAME.debugMode) console.log("Backend not available, retrying in " + PIFRAME.retryFrequency + " milliseconds");
					setTimeout(PIFRAME.init, PIFRAME.retryFrequency);
				}
			}
		});
	}
};

$(document).ready(function() {
	PIFRAME.init();	
});