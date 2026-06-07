const fs = require('fs');
const path = require('path');
const p = path.join(process.cwd(), 'docs/diagrams/class 4.md');
const text = fs.readFileSync(p, 'utf8');
const lines = text.split(/\r?\n/);

function convertParams(params) {
  if (!params.trim()) return '';
  return params.split(/,(?=(?:[^<]*<[^<]*>)*[^<]*$)/).map(param => {
    param = param.trim();
    const m = param.match(/^(.+?)\s+([A-Za-z0-9_]+)$/);
    if (m) return `${m[2]} : ${m[1]}`;
    return param;
  }).join(', ');
}

const out = lines.map(line => {
  const staticMethod = line.match(/^(\s*)([+\-~])static\s+([A-Za-z0-9_<>~]+)\((.*)\)(\s*:\s*.*)?$/);
  if (staticMethod) {
    return `${staticMethod[1]}${staticMethod[2]}static ${staticMethod[3]}(${convertParams(staticMethod[4])})${staticMethod[5] || ''}`;
  }
  const method = line.match(/^(\s*)([+\-~])([A-Za-z0-9_]+)\((.*)\)(\s*:\s*.*)?$/);
  if (method) {
    return `${method[1]}${method[2]}${method[3]}(${convertParams(method[4])})${method[5] || ''}`;
  }
  const attr = line.match(/^(\s*)([+\-~])([^\s]+)\s+([A-Za-z0-9_]+)\s*$/);
  if (attr) {
    return `${attr[1]}${attr[2]}${attr[4]} : ${attr[3]}`;
  }
  return line;
});
fs.writeFileSync(p, out.join('\n'), 'utf8');
console.log('transformed class 4');
