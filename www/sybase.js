function Sybase() {
}

Sybase.prototype = {

	sync: function (successCallback, errorCallback) {
		cordova.exec(successCallback, errorCallback, "Sybase", "sync", []);
	}
};

Sybase.install = function() {
	if(!window.plugins) {
		window.plugins = {};
	}
	
	window.plugins.sybase = new Sybase();
	return window.plugins.sybase;
};

cordova.addConstructor(Sybase.install);