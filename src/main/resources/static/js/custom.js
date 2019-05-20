var ProjectXAPI = ProjectXAPI || {};

ProjectXAPI.api = (function () {

    var cars;
    var tid;

    var getCarData = function () {
        $.ajax({
            url : '/car-data',
            success : function(data) {
                createCarTiles(data);
                tid = setTimeout(getCarLogs, 2000);
            }
        });
    }

    var createCarTiles = function (data) {
        cars = data;
        for (i = 0; i < data.length; i++) {
            var carData = data[i]
            createCarTile(carData);
        }
    }

    var getCarLogs = function() {
        for (i = 0; i < cars.length; i++) {
            var logCarUrl = "/car-logs/?car=" + i;
            $.ajax({
                url : logCarUrl,
                success : function(data) {
                    console.log("log for car: " + logCarUrl + " : " + data);
                },
                error: function(request,status,errorThrown) {
                    stopCarLogs();
               }
            });
        }
        tid = setTimeout(getCarLogs, 2000); // repeat myself
    }

    function stopCarLogs() { // to be called when you want to stop the timer
        clearTimeout(tid);
    }

    var createCarTile = function(carData) {
        console.log("Creating tile for: " + carData);
        var tile = $('<div id="' + ("car"  + carData.index) + '" class="grid-item"></div>)');
        tile.id = "car" + carData.index;
        tile.class = "grid-item";
        $(tile).attr['class', 'grid-item'];
        $(tile).attr['id', 'car' + carData.index];
        var streamImageUrl = "http://" + carData.ipAddress + ":8080" + "/?action=stream";
        console.log("streamImageUrl: " + streamImageUrl);
        tile.css("background-image", "url('" + streamImageUrl + "')");
        tile.css("background-repeat", "no-repeat");
        tile.css("background-size", "cover");
        tile.css("background-position", "center top");

        var details = $('<div id="' + ("details"  + carData.index) + '"></div>)');
        details.css("background-color", "yellow");
        // details.css("position", "absolute"); 
        details.css("bottom", "0"); 
        details.css("right", "0"); 

        tile.append(details);
        
        $(".grid-container").append(tile);
    }

    var setVideoInDiv = function(ele, streamImageUrl) {
        console.log("Setting stream url: " + streamImageUrl + " for: " + ele)
        ele.css("background-image", "url('" + streamImageUrl + "')");
        ele.css("background-repeat", "no-repeat");
        ele.css("background-size", "cover");
        ele.css("background-position", "center top");
    }

    var init = function () {
        getCarData();
    };
    

    return {
        init: init,
        getCarData: getCarData,
        stopCarLogs: stopCarLogs
    };

})(this)
