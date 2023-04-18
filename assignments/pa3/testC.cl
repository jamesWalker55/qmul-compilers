-- newbasic.cl
-- create basic classes with 'new', which may
-- mess up assumptions, particularly w/bool

class Main {
  io:IO <- new IO;

  main():Object {{
    io.out_string(  ((new Bool)).type_name()  );
  }};
};

