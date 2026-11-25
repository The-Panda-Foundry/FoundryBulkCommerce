#!/usr/bin/env python3
"""Fallback Java 21 compilation using an already prepared, unmodified local NeoForge cache.
Normal development uses ./gradlew build. This helper neither downloads nor alters cache inputs.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[1]
PINS = {
 'CreateNumismatics-1.1.0+neoforge-mc1.21.1.jar': 'd33a33b46f923462ae6806a024851afaefa473ced0e95bf405f8ebc13abce767',
 'numismatics_treasury-1.0.2.jar': 'fde8264ff0e17792b6160e6c41c6ea0b19e5de1343ddddcdf13b5a0bddba46e2',
 'sophisticatedcore-1.21.1-1.5.7.2381.jar': '33ff373cd62e2bef84ca5dd18ffea1c7508842a3156d021f1a0101063f69fb39',
 'sophisticatedstorage-1.21.1-1.6.2.2159.jar': 'bd6cfe04f5062e1ac860f0e8e858185f037b3a20ef3ee387c8f895839781ac69'
}

def archive(path, entries):
    with zipfile.ZipFile(path, 'w', zipfile.ZIP_DEFLATED) as z:
        for name, data in sorted(entries):
            entry = zipfile.ZipInfo(name, (2026, 11, 25, 0, 0, 0))
            entry.compress_type = zipfile.ZIP_DEFLATED
            entry.external_attr = 0o100644 << 16
            z.writestr(entry, data)

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--dependencies', type=Path, default=ROOT/'dependencies')
    parser.add_argument('--gradle-home', type=Path, default=Path.home()/'.gradle')
    parser.add_argument('--java-home', type=Path, default=Path(os.environ.get('JAVA_HOME', '/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home')))
    args = parser.parse_args()
    javac, java = (args.java_home/'bin'/name for name in ('javac', 'java'))
    version = subprocess.check_output([str(javac), '-version'], text=True).strip()
    if not version.startswith('javac 21.'):
        raise SystemExit('A Java 21 JDK is required: '+version)
    deps = []
    for name, expected in PINS.items():
        path = args.dependencies/name
        if hashlib.sha256(path.read_bytes()).hexdigest() != expected:
            raise SystemExit('Dependency fingerprint differs: '+name)
        with zipfile.ZipFile(path) as z:
            if z.testzip(): raise SystemExit('Corrupt dependency: '+name)
        deps.append(path)
    neo = None
    for path in (args.gradle_home/'caches/neoformruntime/intermediate_results').glob('sourcesAndCompiledWithNeoForge*_output.jar'):
        try:
            with zipfile.ZipFile(path) as z:
                meta = z.read('META-INF/neoforge.mods.toml').decode()
                if 'version="21.1.265"' in meta and b'"1.21.1"' in z.read('net/minecraft/SharedConstants.java'):
                    neo = path
                    break
        except (zipfile.BadZipFile, KeyError):
            continue
    if neo is None: raise SystemExit('Prepared NeoForge 21.1.265 / Minecraft 1.21.1 cache not found; run Gradle first.')
    modules = args.gradle_home/'caches/modules-2/files-2.1'
    manifest = json.loads((args.gradle_home/'caches/neoformruntime/artifacts/minecraft_1.21.1_version_manifest.json').read_text())
    userdev = next((modules/'net.neoforged/neoforge/21.1.265').rglob('*-userdev.jar'))
    with zipfile.ZipFile(userdev) as z:
        neo_config = json.loads(z.read('config.json'))
    coordinates = [entry['name'] for entry in manifest['libraries'] if len(entry['name'].split(':')) == 3]
    coordinates += neo_config['libraries'] + ['org.jetbrains:annotations:24.1.0']
    selected = {}
    for coordinate in coordinates:
        group, artifact, version, *classifier = coordinate.split(':')
        name = artifact+'-'+version+('-'+classifier[0] if classifier else '')+'.jar'
        found = list((modules/group/artifact/version).glob('*/'+name))
        if not found: raise SystemExit('Required cached library missing: '+coordinate)
        selected[(group,artifact)] = found[0]
    libs = list(selected.values())
    minecraft_resources = args.gradle_home/'caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar'
    classpath = os.pathsep.join(map(str, [neo, *deps, *libs, minecraft_resources]))
    build = ROOT/'build'
    classes = build/'classes/java/main'
    testclasses = build/'classes/java/test'
    for folder in (classes, testclasses):
        if folder.exists(): shutil.rmtree(folder)
        folder.mkdir(parents=True)
    sources = sorted((ROOT/'src/main/java').rglob('*.java'))
    subprocess.run([str(javac), '--release', '21', '-proc:none', '-sourcepath', '', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(classes), *map(str,sources)], check=True)
    tests = sorted((ROOT/'src/test/java').rglob('*.java'))
    testpath = str(classes)+os.pathsep+classpath
    subprocess.run([str(javac), '--release', '21', '-proc:none', '-sourcepath', '', '-cp', testpath, '-d', str(testclasses), *map(str,tests)], check=True)
    subprocess.run([str(java), '-cp', str(testclasses)+os.pathsep+testpath, 'com.pandafoundry.bulkcommerce.transaction.TransactionTest'], check=True)
    subprocess.run([str(java), '-cp', str(testclasses)+os.pathsep+testpath, 'com.pandafoundry.bulkcommerce.CompatibilityTest', str(args.dependencies)], check=True)
    subprocess.run([str(java), '-cp', str(testclasses)+os.pathsep+testpath, 'com.pandafoundry.bulkcommerce.storage.VanillaTest'], check=True)
    subprocess.run([str(java), '-cp', str(testclasses)+os.pathsep+testpath, 'com.pandafoundry.bulkcommerce.storage.SophisticatedSchemaTest'], check=True)
    lib = build/'libs'
    lib.mkdir(exist_ok=True)
    binary = [(str(p.relative_to(classes)), p.read_bytes()) for p in classes.rglob('*.class')]
    binary += [(str(p.relative_to(ROOT/'src/main/resources')),p.read_bytes()) for p in (ROOT/'src/main/resources').rglob('*') if p.is_file()]
    binary.append(('META-INF/MANIFEST.MF', b'Manifest-Version: 1.0\nFMLModType: MOD\nImplementation-Title: Foundry Bulk Commerce\nImplementation-Version: 0.1.0\n\n'))
    archive(lib/'foundrybulkcommerce-0.1.0.jar', binary)
    archive(lib/'foundrybulkcommerce-0.1.0-sources.jar', [(str(p.relative_to(ROOT/'src/main/java')),p.read_bytes()) for p in sources])
    receipt = {'method':'offline javac fallback; Gradle blocked before project configuration by sandbox socket restrictions',
               'jdk':version,'minecraft':'1.21.1','neoforge':'21.1.265','dependency_sha256':PINS,
               'runtime_status':'Not launched with full mod stack. Staging acceptance required.',
               'jar_sha256':hashlib.sha256((lib/'foundrybulkcommerce-0.1.0.jar').read_bytes()).hexdigest()}
    (build/'verification.json').write_text(json.dumps(receipt,indent=2)+'\n')
    print('Compiled staging candidate:',lib/'foundrybulkcommerce-0.1.0.jar')

if __name__ == '__main__':
    try: main()
    except subprocess.CalledProcessError as error:
        raise SystemExit('Build/check failed with exit code '+str(error.returncode))
