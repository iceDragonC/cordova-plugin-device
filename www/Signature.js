let exec = require('cordova/exec');

var Signature = {
    init(args, success, error) {
        exec(success, error, "Signature", "init", [args]);
    },
    startH5Activity(args, success, error) {
        exec(success, error, "Signature", "startH5Activity", [args]);
    }
}
module.exports = Signature;