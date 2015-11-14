Shader "geronimo" {
	Properties {
		
	}
	SubShader {
		Tags {
			"Queue" = "Transparent"
			
		}
		Pass {
			ZWrite Off
			
			Blend SrcAlpha OneMinusSrcAlpha
			
			GLSLPROGRAM
			
			#ifdef VERTEX
			
			uniform mat4 _Object2World;
			varying vec4 position_in_world_space;
			void main(void){
				(position_in_world_space = (_Object2World * gl_Vertex));
				(gl_Position = (gl_ModelViewProjectionMatrix * gl_Vertex));
				
			}
			
			#endif
			
			
			#ifdef FRAGMENT
			
			varying vec4 position_in_world_space;
			void main(void){
				vec2 v44608;
				float v44611;
				float v44578;
				vec2 v44607;
				float v44566;
				vec2 v44609;
				vec2 v44606;
				vec2 v44612;
				float v44610;
				(v44566 = distance(position_in_world_space,vec4(0.0,0.0,0.0,1.0)));
				(v44578 = (((2.0 * 3.141593) * v44566) * 0.008333334));
				(v44606 = (0.9 * vec2(position_in_world_space[int(0.0)],position_in_world_space[int(1.0)])));
				(v44607 = fract(v44606));
				(v44608 = vec2((2.0 * v44607[int(0.0)]),(2.0 * v44607[int(1.0)])));
				(v44609 = ((v44607 * v44607) * vec2((3.0 - v44608[int(0.0)]),(3.0 - v44608[int(1.0)]))));
				(v44610 = v44609[int(0.0)]);
				(v44611 = v44609[int(1.0)]);
				(v44612 = floor(v44606));
				(gl_FragColor = vec4(0.0,0.0,0.0,((mod(v44566,13.0) / 13.0) - smoothstep((1.0 - (1.0 / v44578)),1.0,(abs((mod((((((((((fract((sin(dot((vec2(0.0) + vec2(v44612)),vec2(127.1,311.7))) * 43758.545312)) * (1.0 - v44610)) + (fract((sin(dot((vec2(1.0,0.0) + vec2(v44612)),vec2(127.1,311.7))) * 43758.545312)) * v44610)) * (1.0 - v44611)) + (((fract((sin(dot((vec2(0.0,1.0) + vec2(v44612)),vec2(127.1,311.7))) * 43758.545312)) * (1.0 - v44610)) + (fract((sin(dot((vec2(1.0) + vec2(v44612)),vec2(127.1,311.7))) * 43758.545312)) * v44610)) * v44611)) * 2.0) - 1.0) / v44578) * 2.0) + (mod(((atan(position_in_world_space[int(0.0)],position_in_world_space[int(1.0)]) + 3.141593) / (3.141593 * 2.0)),0.008333334) / 0.008333334)),1.0) - 0.5)) * 2.0)))));
				
			}
			
			#endif
			
			ENDGLSL
		}
		
	}
	
}

