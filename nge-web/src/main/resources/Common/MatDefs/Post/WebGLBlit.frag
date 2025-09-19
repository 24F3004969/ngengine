#import "Common/ShaderLib/GLSLCompat.glsllib"
#import "Common/ShaderLib/MultiSample.glsllib"
#import "Common/ShaderLib/ColorSpace.glsllib"

uniform COLORTEXTURE m_Texture;
varying vec2 texCoord;

 
void main() {
    vec4 texVal = getColor(m_Texture, texCoord);
    texVal.rgb = linearToSrgb(texVal.rgb);
    gl_FragColor = texVal;
}