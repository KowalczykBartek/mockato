function setCookie(name,value,days) {
    var expires = "";
    if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days*24*60*60*1000));
        expires = "; expires=" + date.toUTCString();
    }
    document.cookie = name + "=" + (value || "")  + expires + "; path=/";
}

function getCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for(var i=0;i < ca.length;i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1,c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
    }
    return null;
}

//set subdomain random id if not in the cookie already.
var subdomain = getCookie("random_subdomain")
if(subdomain == null) {
    var timestamp = new Date().getUTCMilliseconds();
    setCookie("random_subdomain", generateUID(20), 999)
}

function generateUID(length)
{
    return window.btoa(Array.from(window.crypto.getRandomValues(new Uint8Array(length * 2))).map((b) => String.fromCharCode(b)).join("")).replace(/[+/]/g, "").substring(0, length);
}

var subdomain = getCookie("random_subdomain")

console.log("context's subdomain " + subdomain)

//set domain for navbar
$(document).ready(function() {
    $("#show-domain-input").val(subdomain)
})
$(document).ready(function() {
    $("#new-domain").bind("click", function() {
        var timestamp = new Date().getUTCMilliseconds();
        setCookie("random_subdomain", "sub_" + timestamp, 999)
        window.location.replace("/")
    });
})