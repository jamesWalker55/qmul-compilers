(*
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
*)
--class inherits true false
--let in
--case of esac
--if then else fi
--while loop pool
--new isvoid
--not
--Int String Bool IO SELF_TYPE Object
--out_string out_int
--in_string in_int
--abort
--type_name
--copy
--length concat substr
--init
--{
--}
----
--(**)
--""
--: ; <-  ( ) + - * / < <= = . @ ~ ,