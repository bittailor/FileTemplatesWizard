// file : ${global.filename}
// date : ${global.date?string("dd.MM.yy")}
// date : ${global.user}
// env.PATH : ${env.PATH}


#ifndef INC__${global.filename?replace(".","__")?upper_case}
#define INC__${global.filename?replace(".","__")?upper_case}

class ${string.class} 
{
   public:
      ${string.class}();
      ~${string.class}();
   
   private:
   
};

#endif // INC__${global.filename?upper_case}