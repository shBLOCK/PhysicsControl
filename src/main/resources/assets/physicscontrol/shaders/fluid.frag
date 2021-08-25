#version 430

uniform uint count;
uniform float size;
uniform vec2 screenSize;
uniform vec2 translate;
uniform float scale;
uniform samplerBuffer data;

out vec4 color;

#define TEXEL_PER 3

vec2 toSpacePos(vec2 pos) {
    return vec2(
        (pos.x - translate.x) / scale,
        -((pos.y - translate.y) / scale)
    );
}

void main() {
    vec2 currentPos = toSpacePos(vec2(gl_FragCoord.x, screenSize.y - gl_FragCoord.y));
    vec4 sum = vec4(0.0);
    float facSum = 0.0;
    for (int i=0; i<count; i++) {
        vec4 v1 = texelFetch(data, i * TEXEL_PER);

        vec2 pos = vec2(v1.xy);
//        vec2 offset = abs(pos - currentPos);
//        if (offset.x + offset.y > 4.0) {
//            continue;
//        }

        vec4 v2 = texelFetch(data, i * TEXEL_PER + 1);
        vec4 v3 = texelFetch(data, i * TEXEL_PER + 2);

        vec4 col = vec4(v1.zw, v2.xy);
        vec2 vel = vec2(v2.zw);
        int flags = int(v3.x);

//        float factor = size / distance(pos, currentPos);
        float factor = 1.0 - smoothstep(size * 1.0, size * 4.0, distance(pos, currentPos));
//        factor *= smoothstep(0.5, 1.5, factor);
        //        factor = smoothstep(1.0, 100.0, factor);
        facSum += factor;
        sum += col * vec4(factor);
    }
    color = sum;
    if (facSum > 1.0) {
        color /= facSum;
    } else {
        color.a *= step(0.8, color.a);
    }
//    color = clamp(color, 0.0, 1.0);
}