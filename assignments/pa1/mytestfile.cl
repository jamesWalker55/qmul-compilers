class Main inherits IO {
    main() : Object {
        out_string("booltoS(true || false))
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
