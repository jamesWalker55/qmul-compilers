class Main inherits IO {
    main() : Object {
        out_string("test")
    };

	return:String <- "";
    booltoS(b:Bool) : String {
        {
            if b = true
				then
					return <- "true"
				else
					return <- "false"
			fi;
			return;
        }
    };
};
