module.exports = {
  parserPreset: {
    parserOpts: {
      headerPattern: /^(\w+)(\([A-Z]+-\d+\))?:\s(.+)$/,
      headerCorrespondence: ['type', 'ticket', 'subject']
    }
  },
  rules: {
    'type-enum': [2, 'always', ['feat', 'fix', 'chore', 'docs', 'refactor', 'test', 'style', 'perf']],
    'type-empty': [2, 'never', '❌ Debe indicar tipo: feat, fix, chore, docs, etc.'],
    'subject-empty': [2, 'never', '❌ El mensaje no puede estar vacío']
  }
};
