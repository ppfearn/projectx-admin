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
            
            let car = i + 1;
            var logCarUrl = "/car-logs/?car=" + i;
            // var logCarUrl = "/car-logs-test/?car=" + car;
            $.ajax({
                url : logCarUrl,
                success : function(data) {
                    console.log("log for car: " + logCarUrl + " : " + data + " car: " + car);
                    $("#car" + car + "-direction").text(data.replace("action=", ""));
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
        var tile = $("#car"  + carData.index);
        let car = carData.index;
        // Set CSS for tile
        var streamImageUrl = "http://" + carData.ipAddress + ":8080" + "/?action=stream";
        // var streamImageUrl = "http://localhost:8080/test-stream";
        console.log("streamImageUrl: " + streamImageUrl);
//        tile.css("background-image", "url('" + streamImageUrl + "')");
//        tile.css("background-repeat", "no-repeat");
//        tile.css("background-size", "cover");
//        tile.css("background-position", "center top");
        $("#car"  + carData.index).attr("src",streamImageUrl);

        $("#car" + car + "-team-name").text(carData.teamName);
        $("#car" + car + "-logo").attr("src","/car-logo/?car=" + car);
        $("#car" + car + "-flag").attr("src","/car-flag/?car=" + car);

    }

    var flagForCar = function(car) {

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
