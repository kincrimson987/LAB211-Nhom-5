const fs = require('fs');
const path = require('path');
const files = [
  'docs/diagrams/class 4.md',
  'docs/diagrams/class 5.md',
  'docs/diagrams/class 6.md',
  'docs/diagrams/class 7.md',
  'docs/diagrams/class login.md'
];

function convertParams(params) {
  if (!params.trim()) return '';
  return params.split(/,(?=(?:[^<]*<[^>]*>)*[^<]*$)/).map(param => {
    param = param.trim();
    const m = param.match(/^(.+?)\s+([A-Za-z0-9_]+)$/);
    if (m) return `${m[2]} : ${m[1]}`;
    return param;
  }).join(', ');
}

files.forEach(relativePath => {
  const fullPath = path.join(process.cwd(), relativePath);
  if (!fs.existsSync(fullPath)) {
    console.error('Missing file', relativePath);
    return;
  }
  const text = fs.readFileSync(fullPath, 'utf8');
  const lines = text.split(/\r?\n/);
  let currentClass = null;
  const out = lines.map(line => {
    const classMatch = line.match(/^class\s+([A-Za-z0-9_]+)(?:~[^~]+~)?\s*\{/);
    if (classMatch) {
      currentClass = classMatch[1];
      return line;
    }
    if (/^\s*\}/.test(line)) {
      currentClass = null;
      return line;
    }

    const staticMethod = line.match(/^(\s*)([+\-~])static\s+([^\s]+)\s+([A-Za-z0-9_]+)\((.*)\)(?:\s*:\s*(.*))?\s*$/);
    if (staticMethod) {
      const prefix = staticMethod[1];
      const visibility = staticMethod[2];
      const returnType = staticMethod[3];
      const methodName = staticMethod[4];
      const params = convertParams(staticMethod[5]);
      const suffix = staticMethod[6] ? ` : ${staticMethod[6]}` : ` : ${returnType}`;
      return `${prefix}${visibility}static ${methodName}(${params})${suffix}`;
    }

    const constructorLine = line.match(/^(\s*)([+\-~])([A-Za-z0-9_]+)\((.*)\)\s*$/);
    if (constructorLine && currentClass && constructorLine[3] === currentClass) {
      const prefix = constructorLine[1];
      const visibility = constructorLine[2];
      const name = constructorLine[3];
      const params = convertParams(constructorLine[4]);
      return `${prefix}${visibility}${name}(${params})`;
    }

    const method = line.match(/^(\s*)([+\-~])([^\s]+)\s+([A-Za-z0-9_]+)\((.*)\)(?:\s*:\s*(.*))?\s*$/);
    if (method) {
      const prefix = method[1];
      const visibility = method[2];
      const returnType = method[3];
      const methodName = method[4];
      const params = convertParams(method[5]);
      const suffix = method[6] ? ` : ${method[6]}` : ` : ${returnType}`;
      return `${prefix}${visibility}${methodName}(${params})${suffix}`;
    }

    const attr = line.match(/^(\s*)([+\-~])([^\s]+)\s+([A-Za-z0-9_]+)\s*$/);
    if (attr) {
      return `${attr[1]}${attr[2]}${attr[4]} : ${attr[3]}`;
    }
    return line;
  });
  fs.writeFileSync(fullPath, out.join('\n'), 'utf8');
  console.log('transformed', relativePath);
});
