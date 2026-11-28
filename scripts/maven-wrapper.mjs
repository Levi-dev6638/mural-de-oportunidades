import { spawn } from 'node:child_process';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const repositoryRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const windows = process.platform === 'win32';
const mavenArguments = process.argv.slice(2);

if (!mavenArguments.length || mavenArguments.some((argument) => !/^[A-Za-z0-9_./:=+-]+$/.test(argument))) {
  console.error('Provide Maven goals or options using letters, numbers, and . / : = + - _ characters only.');
  process.exit(2);
}

const command = windows ? process.env.ComSpec || 'cmd.exe' : 'bash';
const args = windows
  ? ['/d', '/s', '/c', `mvnw.cmd ${mavenArguments.join(' ')}`]
  : ['./mvnw', ...mavenArguments];
const child = spawn(command, args, {
  cwd: path.join(repositoryRoot, 'Back-end'),
  stdio: 'inherit',
  windowsHide: true,
});

child.on('error', (error) => {
  console.error(`Could not start Maven Wrapper: ${error.message}`);
  process.exitCode = 1;
});

child.on('exit', (code) => {
  process.exitCode = code ?? 1;
});
