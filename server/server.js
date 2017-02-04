var express = require('express');
var bodyParser = require('body-parser');
var app = express();
var crypto = require("crypto");

var requestLogger = function(req, res, next) {
    console.log('**************************************************************');
    console.log("request logger url:" + req.url);
    if (req.method.localeCompare("POST") == 0) {
    	printPostParam(req);
    } 
    res.writeHead(200, {'Content-Type': 'text/plain'});
    next();
}

function printPostParam (req) {
	var postBody = "";
	for (var param in req.body) {
		postBody += param;
		postBody += ":";
		postBody += req.body[param];
		postBody += ",";
    }
    console.log("request logger post body:" + postBody);
}

function calMd5 (data) {
     var singResult = crypto.createHash('md5').update(data.toString()).digest('base64');
     console.log("request param sign:"+singResult);
     return singResult;
};

var signatureChecker = function (req, res, next) {
    console.log('**************************************************************');
    var keys;
    if (req.method.localeCompare("GET") == 0) {
    	keys = req.query;
    } else if (req.method.localeCompare("POST") == 0) {
    	keys = req.body;
    }
    var sorted = Object.keys(keys) 
        .sort(
            function(a,b) { 
            return a.localeCompare(b);
        });
        var finalStr = "";
        for (var param in sorted) {
            if (sorted[param].localeCompare("sign") == 0) {
                continue;
            }
            finalStr += sorted[param];
            if (req.method.localeCompare("GET") == 0) {
    			finalStr += req.query[sorted[param]];
    		} else if (req.method.localeCompare("POST") == 0) {
    			finalStr += req.body[sorted[param]];
    		}
        }
        var sign;
        if (req.method.localeCompare("GET") == 0) {
    		sign = req.query['sign'];
    	} else if (req.method.localeCompare("POST") == 0) {
    		sign = req.body['sign'];
    	}

        console.log("sign in url:" + sign);
        if (sign.localeCompare(calMd5(new Buffer(finalStr))) != 0) {
          console.log('sign did not match');
          var result = {
                "error_code" : 1005,
                "error_desc" : "sign did not match"
          }
          res.end(JSON.stringify(result));
        } else {
          console.log('sign match do next');
          next();
        }  
};

function cbcDecrypt(data) {
    var cipher = crypto.createDecipher('aes-128-ecb',"1234fghjnmlkiuhA");  
    return cipher.update(data.toString(),'hex','utf8') + cipher.final('utf8');  
}

var paramDecrypt = function (req, res, next) {
    console.log('************************************************************************')
    if (req.method.localeCompare("GET") == 0) {
    	var decryptResult="";
    	for (var param in req.query) {
        	if (param.localeCompare('sign') == 0) {
          		continue;
        	}
        	req.query[param] = cbcDecrypt(new Buffer(req.query[param], 'base64', 'hex'));
        	decryptResult += param;
        	decryptResult += ":";
        	decryptResult += req.query[param];
        	decryptResult += ",";
    	}
    	console.log('decryptResult:' + decryptResult);
    } else if (req.method.localeCompare("POST") == 0) {
    	var decryptResult="";
    	for (var param in req.body) {
        	if (param.localeCompare('sign') == 0) {
          		continue;
        	}
        	req.body[param] = cbcDecrypt(new Buffer(req.body[param], 'base64', 'hex'));
        	decryptResult += param;
        	decryptResult += ":";
        	decryptResult += req.body[param];
        	decryptResult += ",";
    	}
    	console.log('decryptResult:' + decryptResult);
    }

    next();
};

var tokenChecker = function(req, res, next) {
     console.log('************************************************************************')
    var token_str;
    if (req.method.localeCompare("GET") == 0) {
    	 token_str = req.query['token'];
    } else if (req.method.localeCompare("POST") == 0) {
    	 token_str = req.body['token'];
    }
     if (token_str) {
         var token_time = parseFloat(token_str);
         var cur_time = new Date().getTime();
         if(cur_time - token_time < 30 * 1000){
          console.log('token check pass, do next token_str:' + token_str + ', server_token:' + cur_time + 'diff:' + (cur_time - token_time));
          next();
       } else {
          console.log('token expired, please invalidate token_str:' + token_str + ', server_token:' + cur_time + 'diff:' + (cur_time - token_time));
          res.end(JSON.stringify({"error_desc": "token expired", "error_code" : 1001})); 
       }
     } else {
          console.log('token empty, please invalidate');
          res.end(JSON.stringify({"error_desc": "token empty", "error_code" : 1001})); 
     }
};

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(requestLogger);
app.use(signatureChecker);
app.use(paramDecrypt);
app.use(tokenChecker);

app.all('/request', function (req, res) {
    console.log('************************************************************************')
    var result = {
		"error_code" : 0,
		"error_desc" : "success",
        "data" : {
            "response_server" : "来自Nodejs服务器的问候"
        }
    }
    res.end(JSON.stringify(result)); 
});

app.listen(8888, function () {
  console.log('Server running at http://127.0.0.1:8888/');
});