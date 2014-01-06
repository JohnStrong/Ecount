var ecount = angular.module('Ecount', ['ngRoute'],

	function($routeProvider, $locationProvider) {

		$routeProvider.when('/', {
			redirectTo: '/feature/home'
		});
		$routeProvider.when('/feature/home', {
			templateUrl: '/home',
			controller: HomeController,
			controllerAs: 'home'
		});
		$routeProvider.when('/feature/map', {
			templateUrl: '/map',
			controller: MapController,
			controllerAs: 'map'
		});
		$routeProvider.when('/feature/about', {
			templateUrl: '/about',
			controller: AboutController,
			controllerAs: 'about'
		});

	// configure html5 to get links working on jsfiddle
	$locationProvider.html5Mode(true);
});

ecount.factory('Counties', function($http) {
	return $http.get('/tallysys/map/counties');
});

ecount.factory('CountyBounds', function() {
	var COUNTY_BOUNDS_REQ_URL = '/tallysys/map/county/';

	return function(county) {
		return $.getJSON(COUNTY_BOUNDS_REQ_URL + county);
	};
});

ecount.factory('ElectionBounds', function($http) {

	var ELECTION_BOUNDS_REQ_URL = '/tallysys/map/divisions/';

	return function(countyId) {
		return $http.get(ELECTION_BOUNDS_REQ_URL + countyId);
	};
});

ecount.factory('ElectionStatistics', function($http) {
	var GENERAL_ELECTION_STATS_URL = '/stats/elections/general/';

	return function(countyId) {
		return $http.get(GENERAL_ELECTION_STATS_URL + countyId);
	};
});

ecount.service('Map', function(ElectionBounds, ElectionStatistics) {

	var DEFAULT_ZOOM_LEVEL = 7;
	var EVENT_ZOOM_LEVEL = 8;

	var map = L.map('imap-view');
	var geojson;

	var info = (function() {

		var i = L.control();

		i.onAdd = function (map) {
		    this._div = L.DomUtil.create('div', 'imap-info'); // create a div with a class "info"
		    this.update();
		    return this._div;
		};

		// method that we will use to update the control based on feature properties passed
		i.update = function (stats) {
			var htm = '<p>click a county to view some election statistics</p>';

			if(stats !== undefined) {
				htm = ''
			}

			$(stats).each(function(key, data) {
				htm += '<div class="imap-info-entry">' +
				'<b>' + data.constituency + ': </b>' +
				'<p>' +
				'registered electors: ' + data.registeredElectors + '<br />' +
				'votes: ' + data.votes + '<br />' +
				'turnout: ' + data.percentTurnout + '%<br />' +
				'invalid ballots: ' + data.invalidBallots + '<br />' +
				'percentage invalid: ' + data.percentInvalid + '%<br />' +
				'valid ballots: ' + data.validVotes + '<br />' +
				'percentage valid: ' + data.percentValid + '%<br />' +
				'</p>' +
				'</div>';
			});

		    this._div.innerHTML = '<h4>Election statistics</h4>' + htm;
		};

		i.addTo(map);

		return i;
	})();

	var layer = (function() {
		var URL = 'http://{s}.tile.cloudmade.com/{key}/22677/256/{z}/{x}/{y}.png';
		var ATTRIBUTION = 'Map data &copy; 2011 OpenStreetMap contributors, ' +
			'Imagery &copy; 2012 CloudMade';
		var API_KEY = '1f43dc838a3344c69e1a320cf87ce237';

		L.tileLayer(URL, {
			attribution: ATTRIBUTION,
			key: API_KEY
		}).addTo(map);
	})();

	var setView = function(position) {
		map.setView([position.coords.latitude, position.coords.longitude], DEFAULT_ZOOM_LEVEL);
	};

	var style = function(feature) {

		var DEFAULT_WEIGHT = 2;
        var DEFAULT_OPACITY = 0.5;
        var DEAULT_COLOR = '#888888';
        var DEAULT_FILL_COLOR = '#265588';
        var DEAULT_DASH_ARRAY = '3';
        var DEFAULT_FILL_OPACITY = 0.2;

	    return {
	        weight: DEFAULT_WEIGHT,
	        opacity: DEFAULT_OPACITY,
	        color: DEAULT_COLOR,
	        fillColor: DEAULT_FILL_COLOR,
	        dashArray: DEAULT_DASH_ARRAY,
	        fillOpacity: DEFAULT_FILL_OPACITY
	    };
	};

	var interaction = function(feature, layer) {


		var HIGHLIGHT_WEIGHT = 3;
		var HIGHLIGHT_CLICK_COLOR = '#885526';
		var HIGHLIGHT_FILL_OPACITY = 0.5;

		function highlightFeature(e) {
			var layer = e.target;

			layer.setStyle({
				weight: HIGHLIGHT_WEIGHT,
		        color: HIGHLIGHT_CLICK_COLOR,
		        fillOpacity: HIGHLIGHT_FILL_OPACITY
			});

			if(!L.Browser.ie && !L.Browser.opera) {
       			layer.bringToFront();
   			}
		}

		function resetHighlight(e) {
			geojson.resetStyle(e.target);
		}

		function getElectoralInformation(e){

			function zoomToFeature() {
				map.fitBounds(e.target.getBounds());
			}

			function drawED(geom) {
				L.geoJson(geom, {
					style: style
				}).addTo(map);
			}

			var id = feature.properties.id;

			// electoral divisions
			ElectionBounds(id).success(function(geom) {
				zoomToFeature();
				drawED(geom);
			}).error(function(err) {
				// defer error
			});

			// election stats of county
			ElectionStatistics(id).success(function(stats){
				info.update(stats);
			}).error(function(err) {
				// defer error
			});
		}

		layer.on({
			mouseover: highlightFeature,
			mouseout: resetHighlight,
			click: getElectoralInformation
		});
	}

	return {

		renderMap: function() {
			navigator.geolocation.getCurrentPosition(setView);
		},

		drawCounties: function (collection) {
			geojson = L.geoJson(collection, {
				style: style,
				onEachFeature: interaction
			}).addTo(map);
		}
	};
});

function MapController($scope, Map, Counties, CountyBounds) {

	$scope.renderMap = function() {
		Map.renderMap();
	};

	var buildMap = function(county) {
		$.when(CountyBounds(county.name))
			.done(function(countyBounds) {
				Map.drawCounties(countyBounds);
			});
	};

	Counties.success(function(data) {

		counties = data.counties;
		$scope.counties = counties;

		$.each(data.counties, function(key, county) {
			buildMap(county);
		});

	}).error(function(err) {
		// defer error
	});
}

/** STATISTICS SERVICE **/
ecount.factory('GeneralStats', function($http){

	return {
		liveRegister: function() {
			return $.getJSON(
				'/stats/general/register/mature/m',
				'/stats/general/register/mature/f',
				'/stats/general/register/young/m',
				'/stats/general/register/young/f')
		}
	};
});

function StatBankController($scope, GeneralStats) {
	$.when(GeneralStats.liveRegister())
		.done(function(mm, mf, ym, yf) {
			console.log(mm, mf, ym, yf);
		});
}

function AboutController($scope) {

}

function HomeController($scope) {

}