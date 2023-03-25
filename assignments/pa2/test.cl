--This is a test file to check minimum viable product
class Main { main() : Object {0}; };

class A {
      bar() : Object {
	    (new B).foo(self, 29)
      };
};

class B inherits A {
      foo(b:B, x:Int) : String { "moo" };
};


