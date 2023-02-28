class Test {
  foo:Int;
  bar():Object{foo <- 3 };

  -- WORKING
  --foo:Int;

  --foo:Int <- 3;
  --bar():Object{3};

  -- foo:Int <- 3;
  -- bar():Object{foo};

  -- foo:Int;
  -- bar():Object{foo <- 3 };

  -- main() : Object {
  --   true
  -- };
  

  -- TO DO
  -- cow(a:Int) : SELF_TYPE {
  --   self
  -- };
};