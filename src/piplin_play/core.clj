(ns piplin-play.core
  (:require [piplin.core :as p]
            [plumbing.core :refer [fnk]]))

(def counter-module
  (p/modulize :counter
    {:output (fnk [output]
               (p/inc output))}
    {:output ((p/uintm 8) 0)}))


(def reset-counter-module
  (p/modulize :reset-counter
              ; computation
              {:output (fnk [reset output]
                            (p/cond (p/= reset true) ((p/uintm 10) 0)
                                    :else (p/inc output)))}
              ; initial state
              {:output ((p/uintm 10) 0)}))


(def test-module
  (p/modulize :test-module
              {:sub-counter (fnk []
                                 (counter-module))
               :reset-counter (fnk [reset]
                                   (reset-counter-module :reset reset))
               :reset (fnk [sub-counter]
                            (p/condp p/= (:output sub-counter)
                              3 true
                              5 true
                              8 true
                              false))}
              {:reset false}))

(p/sim (p/compile-root test-module) 10)
; =>
(comment
  [{[:test-module :reset-counter :output] AST(UIntM[10], 0),
    [:test-module :counter :output] AST(UIntM[8], 0),
    [:test-module :reset] false}
   {[:test-module :reset-counter :output] AST(UIntM[10], 1),
    [:test-module :counter :output] AST(UIntM[8], 1),
    [:test-module :reset] false}
   {[:test-module :reset-counter :output] AST(UIntM[10], 2),
    [:test-module :counter :output] AST(UIntM[8], 2),
    [:test-module :reset] false}
   {[:test-module :reset-counter :output] AST(UIntM[10], 3),
    [:test-module :counter :output] AST(UIntM[8], 3),
    [:test-module :reset] false}
   {[:test-module :reset-counter :output] AST(UIntM[10], 4),
    [:test-module :counter :output] AST(UIntM[8], 4),
    [:test-module :reset] true}
   {[:test-module :reset-counter :output] AST(UIntM[10], 0),
    [:test-module :counter :output] AST(UIntM[8], 5),
    [:test-module :reset] false}
   {[:test-module :reset-counter :output] AST(UIntM[10], 1),
    [:test-module :counter :output] AST(UIntM[8], 6),
    [:test-module :reset] true}
   {[:test-module :reset-counter :output] AST(UIntM[10], 0),
    [:test-module :counter :output] AST(UIntM[8], 7),
    [:test-module :reset] false}
   {[:test-module :reset-counter :output] AST(UIntM[10], 1),
    [:test-module :counter :output] AST(UIntM[8], 8),
    [:test-module :reset] false}
   {[:test-module :reset-counter :output] AST(UIntM[10], 2),
    [:test-module :counter :output] AST(UIntM[8], 9),
    [:test-module :reset] true}
   {[:test-module :reset-counter :output] AST(UIntM[10], 0),
    [:test-module :counter :output] AST(UIntM[8], 10),
    [:test-module :reset] false}])


(p/->verilog (p/compile-root test-module) {})
; =>
"module piplin_module(\n
  clock,\n
);\n
 input wire clock;\n
//Input and output declarations\n\n

//Registers\n
 reg  reset9409 = 1'b0;\n
 reg [9:0] output9410 = 10'd0;\n
 reg [7:0] output9411 = 8'd0;\n\n

//Main code\n
 wire  G__9412 = 8'd8 == output9411;\n
 wire  G__9413 = G__9412 ? 1'b1 : 1'b0;\n
 wire  G__9414 = 8'd5 == output9411;\n
 wire  G__9415 = G__9414 ? 1'b1 : G__9413;\n
 wire  G__9416 = 8'd3 == output9411;\n
 wire  G__9417 = G__9416 ? 1'b1 : G__9415;\n
 wire [9:0] G__9418 = output9410 + 10'd1;\n
 wire  G__9419 = reset9409 == 1'b1;\n
 wire [9:0] G__9420 = G__9419 ? 10'd0 : G__9418;\n
 wire [7:0] G__9421 = output9411 + 8'd1;\n\n

//Assignments to outputs\n\n

 always @(posedge clock) begin\n
  reset9409 <= G__9417;\n
  output9410 <= G__9420;\n
  output9411 <= G__9421;\n
 end\n
endmodule\n"
