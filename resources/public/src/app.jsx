/** @jsx React.DOM */

var app = app || {};

(function () {
    'use strict';

    var Choice = React.createClass({
        render: function () {
            return (
                <div className={'choice ' + this.props.type}>
                    <button className='btn btn-default btn-lg value'>
                        {this.props.value}
                    </button>
                </div>
            );
        }
    });

    var Score = React.createClass({
        render: function () {
            var correct = this.props.correct;
            var total = this.props.total;
            return (
                <div className='score'>
                    <span className='badge'>{correct} / {total}</span>
                </div>
            );
        }
    });

    var Quiz = React.createClass({
        getInitialState: function () {
            return {choices: [],
                    showAnswer: false,
                    total: 0,
                    correct: 0
            };
        },
        refreshChoices: function () {
            $.get("/quiz", function (data) {
                this.setState({choices: data, showAnswer: false});
            }.bind(this));
        },
        incScore: function(correct) {
            this.setState({total: this.state.total + 1,
                           correct: this.state.correct + (correct ? 1 : 0)}
            );
        },
        componentDidMount: function() {
            this.refreshChoices();
        },
        setShowAnswer: function (bool) {
            this.setState({showAnswer: bool});
        },
        handleFake: function (e) {
            this.handleResponse(e, false);
        },
        handleReal: function (e) {
            this.handleResponse(e, true);
        },
        handleResponse: function (e, correct) {
            e.preventDefault();

            if (this.state.showAnswer) {
                this.refreshChoices();
            } else {
                this.setShowAnswer(true);
                this.incScore(correct);
            }
        },
        render: function () {
            var quiz = this;

            return (
                <div className={this.state.showAnswer ? 'showAnswer' : 'hideAnswer'}>
                    {this.state.choices.map(function (c) {
                        return (
                            <form onSubmit={c.type === 'real' ? quiz.handleReal : quiz.handleFake} key={c.value}>
                                <Choice value={c.value} type={c.type} />
                            </form>
                        );
                    })}

                    <Score correct={this.state.correct} total={this.state.total} />
                </div>
            );
        }
    });

    React.renderComponent(<Quiz />, document.getElementById('app'));
})();
