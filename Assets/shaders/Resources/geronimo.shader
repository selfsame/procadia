Shader "geronimo" {
	Properties {
		
	}
	SubShader {
		Pass {
			GLSLPROGRAM
			
			#ifdef VERTEX
			
			varying vec4 position_in_world_space;
			void main(void){
				mat4 _Object2World;
				(position_in_world_space = (_Object2World * gl_Vertex));
				(gl_Position = (gl_ModelViewProjectionMatrix * gl_Vertex));
				
			}
			
			#endif
			
			
			#ifdef FRAGMENT
			
			varying vec4 position_in_world_space;
			void main(void){
				vec4 v3422;
				if((distance(position_in_world_space,vec4(0.0,0.0,0.0,1.0)) < 5.0)){
					(v3422 = vec4(0.0,1.0,0.0,1.0));
					
				}
				else {
					(v3422 = vec4(0.3,0.3,0.3,1.0));
					
				}
				(gl_FragColor = v3422);
				
			}
			
			#endif
			
			ENDGLSL
		}
		
	}
	
}

