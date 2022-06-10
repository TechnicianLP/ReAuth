package technicianlp.reauth.authentication.http.server;

enum HttpStatus {

    OK(200),
    Bad_Request(400),
    Not_Found(404),
    Method_Not_Allowed(405),
    Unsupported_Media_Type(415),
    Not_Implemented(501);

    final int code;

    HttpStatus(int code) {
        this.code = code;
    }
}
