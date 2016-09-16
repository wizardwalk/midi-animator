uniform vec4 m_Color;
uniform float m_Height;
uniform float m_Width;
uniform vec4 m_BorderColor;
uniform float m_BorderSize;
uniform float m_Curve;
uniform float m_RadiusH;
uniform float m_RadiusW;
varying vec4 myPos;

// pos = world position of pixel
// w, h = quad's width and height (divided by two)
// rW, rH = desired radius of width and height curves
// (will result in 1/4 of a circle if they're equal)
bool calculate(vec4 pos, float w, float h, float rW, float rH) {
    float x = abs(pos.x - w);
    float y = abs(pos.y - h);
    if (x < w - rW || y < h - rH) {
        return false;
    } else {
        // we have to find a new distance...
        if (pos.x > w)
            x = (pos.x - m_Width) + rW;
        else
            x = pos.x - rW;
        if (pos.y > h)
            y = (pos.y - m_Height) + rH;
        else
            y = pos.y - rH;
        if ((pow(abs(x),m_Curve))/pow(abs(rW),m_Curve)
            + pow(abs(y),m_Curve)/pow(abs(rH),m_Curve) > 1) {
            return true;
        } else {
            return false;
        }
    }
}

void main(){
    vec4 color = vec4(1.0);

    #ifdef HAS_COLOR
        color = vec4(1.0) * m_Color;
    #endif

    #ifdef HAS_BORDER_SIZE
        color = vec4(1.0) * m_BorderColor;
    #endif

float halfW = m_Width/2;
float halfH = m_Height/2;
float radiusH = m_RadiusH;
float radiusW = m_RadiusW;

// round it... if it's outside of our desired "area" we discard the pixel...
#ifdef HAS_CURVE
if (calculate(myPos, halfW, halfH, radiusW, radiusH)) {
    discard;    
} else {
#endif
    #ifdef HAS_BORDER_SIZE
    vec4 newPos = vec4(1.0);
    float newWidth = halfW - m_BorderSize;
    float newHeight = halfH - m_BorderSize;
    if (abs(myPos.x - halfW) < newWidth && abs(myPos.y - halfH) < newHeight) {
        float newRadiusW = (radiusW * newWidth) / halfW;
        float newRadiusH = (radiusH * newHeight) / halfH;
        vec4 newPos = vec4(1.0);
        newPos.x = (((myPos.x - halfW) * halfW) / newWidth) + halfW;
        newPos.y = (((myPos.y - halfH) * halfH) / newHeight) + halfH;
        #ifdef HAS_CURVE
        if (calculate(newPos, halfW, halfH, radiusW, radiusH)) {
            color = vec4(1.0) * m_BorderColor;
        } else {
        #endif
            color = vec4(1.0) * m_Color;
        #ifdef HAS_CURVE
        }
        #endif
    }
    #endif
#ifdef HAS_CURVE
}
#endif

    gl_FragColor = color;
}

