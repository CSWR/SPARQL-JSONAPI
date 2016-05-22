var express = require('express');

var app = express();
app.set('port', process.env.PORT || 3000);

app.get('/:var', function(req, res){
	res.setHeader('Content-Type', 'application/json');
        res.send(JSON.stringify({ status: "ok", time: 0 }));
});

app.get('/:var/:time', function(req, res){
        var time = req.params.time;
        if (time > 5000 || time < 1) {
          time = 1000;
        }
        setTimeout(function() {
          res.setHeader('Content-Type', 'application/json');
          res.send(JSON.stringify({ status: "ok", time: time }));
        }, parseInt(time));

});


// custom 404 page
app.use(function(req, res){ res.type('text/plain');
        res.status(404);
        res.send('404 - Not Found');
});

// custom 500 page
app.use(function(err, req, res, next){ console.error(err.stack);
        res.type('text/plain');
        res.status(500);
        res.send('500 - Server Error');
});

app.listen(app.get('port'), function(){
  console.log( 'Express started on http://localhost:' +
              app.get('port') + '; press Ctrl-C to terminate.' );
});
